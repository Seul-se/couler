package com.chinaunicom.rpc.entity;

public class Task {

    private Integer id;

    private ServerThread serverThread;

    private byte[] data;

    public Task(Integer id,byte[] data,ServerThread serverThread){
        this.id = id ;
        this.data = data;
        this.serverThread = serverThread;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ServerThread getServerThread() {
        return serverThread;
    }

    public void setServerThread(ServerThread serverThread) {
        this.serverThread = serverThread;
    }
}
