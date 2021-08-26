package com.chinaunicom.rpc.util;

import com.chinaunicom.rpc.common.result.AbstractResultManager;
import com.chinaunicom.rpc.common.socket.ClientSocketReader;
import com.chinaunicom.rpc.common.socket.SocketReader;
import com.chinaunicom.rpc.common.socket.SocketWriter;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionManager {

    private static final int CONNECT_IDE_TIME = 60000;

    private int connectionNum;

    protected AbstractResultManager resultManager;

    private ConcurrentHashMap<String, Connection[]> connectionMap = new ConcurrentHashMap<String, Connection[]>();

    public ConnectionManager(int connectionNum,AbstractResultManager resultManager){
        this.connectionNum = connectionNum;
        this.resultManager = resultManager;
    }

    public void send(String host, int port,byte[] message ,int id) throws IOException {
        String key =host + ":" + port;
        Connection[] connections = connectionMap.get(key);
        if(connections == null){
            synchronized (connectionMap){
                connections = connectionMap.get(key);
                if(connections == null){
                    connections = new Connection[connectionNum];
                    connectionMap.put(key,connections);
                }
            }
        }
        int rand = RandomInt.randomInt(connectionNum);
        Connection connection = connections[rand];
        if(connection == null||!connection.isAvaliable()){
            synchronized (connections){
                //如果此时connections被清理，需要重新初始化
                if(!connectionMap.containsValue(connections)){
                    connections = new Connection[connectionNum];
                    connectionMap.put(key,connections);
                }
                connection = connections[rand];
                if(connection == null){
                    connection = new Connection(host,port,resultManager);
                    connections[rand] = connection;
                }else if(!connection.isAvaliable()){
                    connection.reConnect();
                }
            }
        }
        connection.write(message,id);
        if(isCancel){
            autoClean();
        }

    }

    public void close(){
        Iterator<Connection[]> iterator = connectionMap.values().iterator();
        while (iterator.hasNext()){
            Connection[] connections = iterator.next();
            for(Connection connection:connections){
                if(connection!=null){
                    connection.close();
                }
            }
        }
        t.cancel();
        isCancel = true;
    }

    private Lock synObj = new ReentrantLock();
    private boolean isCancel = true;
    private Timer t;
    private List<Connection> ready2Close = new LinkedList<Connection>();
    private void autoClean(){
        synObj.lock();
        if(isCancel) {
            isCancel = false;
            t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    for (Connection connection : ready2Close) {
                        connection.close();
                    }
                    ready2Close.clear();
                    if (connectionMap.isEmpty()) {
                        this.cancel();
                        isCancel = true;
                        return;
                    }
                    Iterator<Connection[]> iterator = connectionMap.values().iterator();
                    while (iterator.hasNext()) {
                        Connection[] connections = iterator.next();
                        boolean isEmpty = true;
                        for (int i = 0; i < connections.length; i++) {
                            if (connections[i] != null) {
                                if (connections[i].getLastActiveTime() + CONNECT_IDE_TIME < TimeUtil.getTime()) {
                                    synchronized (connections) {
                                        ready2Close.add(connections[i]);
                                        connections[i] = null;
                                    }
                                } else {
                                    isEmpty = false;
                                }
                            }
                        }
                        if (isEmpty) {
                            synchronized (connections) {
                                for(Connection connection:connections) {
                                    if(connection!=null){
                                        isEmpty = false;
                                    }
                                }
                                if(isEmpty) {
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }
            }, CONNECT_IDE_TIME, CONNECT_IDE_TIME);
        }
        synObj.unlock();
    }


    static class Connection implements Closeable{




        private long lastActiveTime;

        private SocketWriter socketWriter;

        private SocketReader socketReader;

        private Socket socket;

        private String host;

        private int port;

        private AbstractResultManager resultManager;

        private boolean avaliable = false;

        public long getLastActiveTime() {
            return lastActiveTime;
        }

        public Connection(String host, int port,AbstractResultManager resultManager) throws IOException {
            this.host = host;
            this.port = port;
            this.resultManager = resultManager;
            connect();
            lastActiveTime = TimeUtil.getTime();
        }



        public void write(byte[] data,int id){
            socketWriter.write(data,id);
            lastActiveTime = TimeUtil.getTime();
        }

        public boolean isAvaliable(){
            return avaliable;
        }

        private void connect() throws IOException {
            socket = new Socket(host,port);
            socketReader = new ClientSocketReader(resultManager);
            socketReader.init(socket);
            socketReader.setCloseable(this);
            socketReader.start();
            socketWriter = new SocketWriter();
            socketWriter.init(socket);
            socketWriter.setCloseable(this);
            socketWriter.start();
            avaliable = true;
        }

        @Override
        public void close(){
            try {
                if(socketReader!=null) {
                    socketReader.close();
                }
                if(socketWriter!=null) {
                    socketWriter.close();
                }
                if(socket!=null) {
                    socket.close();
                }
            } catch (Exception e) {
                Logger.error("连接关闭异常:" + host + ":" + port ,e);
            }
            Logger.info("连接关闭:" +  host + ":" + port );
            avaliable = false;
        }

        public void reConnect() throws IOException {
            close();
            connect();
        }

    }
}
