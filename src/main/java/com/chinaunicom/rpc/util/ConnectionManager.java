package com.chinaunicom.rpc.util;

import com.chinaunicom.rpc.common.result.AbstractResultManager;
import com.chinaunicom.rpc.common.socket.ClientSocketReader;
import com.chinaunicom.rpc.common.socket.SocketReader;
import com.chinaunicom.rpc.common.socket.SocketWriter;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

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
    }


    static class Connection implements Closeable{


        private SocketWriter socketWriter;

        private SocketReader socketReader;

        private Socket socket;

        private String host;

        private int port;

        private AbstractResultManager resultManager;

        private boolean avaliable = false;


        public Connection(String host, int port,AbstractResultManager resultManager) throws IOException {
            this.host = host;
            this.port = port;
            this.resultManager = resultManager;
            connect();
        }



        public void write(byte[] data,int id){
            socketWriter.write(data,id);
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
