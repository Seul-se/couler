package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.ClientSocketReader;
import com.chinaunicom.rpc.common.SocketWriter;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.RandomInt;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RPCClient<R,T>  {

    private String host;
    private int port;
    private int connectionNum;
    private Socket[] connections;
    private ClientSocketReader<T>[] socketReaders;
    private SocketWriter<T>[] socketWriters;
    private List<Integer> aviable = new Vector<Integer>();
    private int aviableSize;
    private Timer t ;

    private ThreadLocal objContainer = new ThreadLocal();

    private Serializer<R> serializer;

    private Serializer<T> deserializer;


    public RPCClient(String host, int port, int connectNum, Serializer<R> serializer, Serializer<T> deserializer){
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
        return aviableSize > 0;
    }

    public void open(){
        for(int i=0;i<connectionNum;i++){
            try {
                connections[i] = new Socket(host,port);
                Logger.info("连接[" + i + "]成功:" + host + ":" + port );
                aviable.add(i);
                aviableSize = aviable.size();
                socketReaders[i] = new ClientSocketReader<T>(deserializer);
                socketReaders[i].init(connections[i]);
                socketReaders[i].start();
                socketWriters[i] = new SocketWriter<T>();
                socketWriters[i].setReconnect(true);
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
                Iterator<Integer> it = aviable.iterator();
                while (it.hasNext()) {
                    Integer i = it.next();
                   if(connections[i] == null||!connections[i].isConnected()||connections[i].isClosed()||socketReaders[i]==null
                           ||!socketReaders[i].isAlive()||socketWriters[i]==null||!socketWriters[i].isAlive()){
                       Logger.info("连接" + i + "关闭:" + host + ":" + port );
                       aviableSize--;
                       it.remove();
                       aviableSize = aviable.size();
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
                    if(!aviable.contains(i)){
                        try {
                            connections[i] = new Socket(host,port);
                            Logger.info("重连[" + i + "]成功:" + host + ":" + port );
                            aviable.add(i);
                            aviableSize = aviable.size();
                            if(socketReaders[i]!=null){
                                socketReaders[i].close();
                            }
                            socketReaders[i] = new ClientSocketReader<T>(deserializer);
                            socketReaders[i].init(connections[i]);
                            socketReaders[i].start();
                            if(socketWriters[i]!=null){
                                socketWriters[i].close();
                            }
                            socketWriters[i] = new SocketWriter<T>();
                            socketWriters[i].setReconnect(true);
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

    private static final Integer maxId = Integer.MAX_VALUE / 2;

    private int getId(){
        int id = ids.getAndIncrement();
        if(id > maxId){
            synchronized (ids){
                if(ids.get() > maxId){
                    ids.getAndSet(0);
                }
            }
        }
        return id;
    }

    public T call(R req,int timeout) throws IOException {
        if(aviableSize==0){
            throw new IOException("没有可用连接");
        }
        int rand = RandomInt.RandomInt(aviableSize);
        int i = aviable.get(rand);
        int id = getId();
        Object syncObj = objContainer.get();
        if(syncObj == null ){
            syncObj = new Object();
            objContainer.set(syncObj);
        }
        socketReaders[i].getResultManager().putObj(id,syncObj);
        try {
            synchronized (syncObj) {
                socketWriters[i].write(serializer.serialize(req),id);
                syncObj.wait(timeout);
            }
        } catch (InterruptedException e) {
            Logger.error("线程异常唤醒:" + host + ":" + port ,e);
        }
        T rsp = socketReaders[i].getResultManager().getResult(id);
        if(rsp!=null){
            return rsp;
        }else{
            throw new IOException("获取返回结果超时 id:" + id);
        }
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
        aviable.clear();
        aviableSize = 0;
    }
}
