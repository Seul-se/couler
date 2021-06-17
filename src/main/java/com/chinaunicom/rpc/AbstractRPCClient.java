package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.socket.ClientSocketReader;
import com.chinaunicom.rpc.common.socket.SocketWriter;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.Logger;
import com.chinaunicom.rpc.util.ThreadPool;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AbstractRPCClient<R,T>  {

    protected String host;
    protected int port;
    private int connectionNum;
    private Socket[] connections;
    protected ClientSocketReader<T>[] socketReaders;
    protected SocketWriter<T>[] socketWriters;
    protected List<Integer> available = new Vector<Integer>();
    protected int availableSize;
    private Timer t ;

    protected Serializer<R> serializer;

    protected Serializer<T> deserializer;

    protected ThreadPool threadPool;


    public AbstractRPCClient(String host, int port, int connectNum, Serializer<R> serializer, Serializer<T> deserializer){
        this.host = host;
        this.port = port;
        this.connections = new Socket[connectNum];
        this.connectionNum = connectNum;
        this.socketReaders = new ClientSocketReader[connectNum];
        this.socketWriters = new SocketWriter[connectNum];
        this.serializer = serializer;
        this.deserializer = deserializer;
    }
    public boolean isConnected(){
        return availableSize > 0;
    }

    public void open(){
        for(int i=0;i<connectionNum;i++){
            try {
                connections[i] = new Socket(host,port);
                Logger.info("连接[" + i + "]成功:" + host + ":" + port );
                available.add(i);
                availableSize = available.size();
                socketReaders[i] = new ClientSocketReader<T>(deserializer,threadPool);
                socketReaders[i].init(connections[i]);
                socketReaders[i].start();
                socketWriters[i] = new SocketWriter<T>();
                socketWriters[i].init(connections[i]);
                socketWriters[i].start();
            } catch (IOException e) {
                Logger.error("无法连接到服务器 host:" + host + ":" + port ,e);
            }
        }

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Iterator<Integer> it = available.iterator();
                while (it.hasNext()) {
                    Integer i = it.next();
                   if(connections[i] == null||!connections[i].isConnected()||connections[i].isClosed()||socketReaders[i]==null
                           ||!socketReaders[i].isAlive()||socketWriters[i]==null||!socketWriters[i].isAlive()){
                       Logger.info("连接" + i + "关闭:" + host + ":" + port );
                       availableSize--;
                       it.remove();
                       availableSize = available.size();
                       try {
                           if(socketReaders[i]!=null) {
                               socketReaders[i].close();
                           }
                           if(socketWriters[i]!=null) {
                               socketWriters[i].close();
                           }
                           if(connections[i]!=null) {
                               connections[i].close();
                           }
                       } catch (Exception e) {
                           Logger.error("连接" + i + "关闭异常:" + host + ":" + port ,e);
                       }

                   }

                }

                for(int i=0;i<connectionNum;i++){
                    if(!available.contains(i)){
                        try {
                            connections[i] = new Socket(host,port);
                            Logger.info("重连[" + i + "]成功:" + host + ":" + port );
                            available.add(i);
                            availableSize = available.size();
                            if(socketReaders[i]!=null){
                                socketReaders[i].close();
                            }
                            socketReaders[i] = new ClientSocketReader<T>(deserializer,threadPool);
                            socketReaders[i].init(connections[i]);
                            socketReaders[i].start();
                            if(socketWriters[i]!=null){
                                socketWriters[i].close();
                            }
                            socketWriters[i] = new SocketWriter<T>();
                            socketWriters[i].init(connections[i]);
                            socketWriters[i].start();
                        } catch (Exception e) {
                            Logger.error("连接" + i + "重连失败异常:" + host + ":" + port ,e);
                        }
                    }
                }
            }
        }, 10000, 10000);
    }

    AtomicInteger ids = new AtomicInteger(0);

    private static final Integer MAX_ID = Integer.MAX_VALUE / 2;

    protected int getId(){
        int id = ids.getAndIncrement();
        if(id > MAX_ID){
            synchronized (ids){
                if(ids.get() > MAX_ID){
                    ids.getAndSet(0);
                }
            }
        }
        return id;
    }

    public void close(){
        t.cancel();
        for(int i=0;i<connectionNum;i++){
            if(socketWriters[i]!=null) {
                socketWriters[i].close();
            }
            if(socketReaders[i]!=null) {
                socketReaders[i].close();
            }
            try {
                if(connections[i]!=null) {
                    connections[i].close();
                }
            } catch (IOException e) {
                Logger.error("连接关闭失败:" + host + ":" + port ,e);
            }
        }
        available.clear();
        availableSize = 0;
    }
}
