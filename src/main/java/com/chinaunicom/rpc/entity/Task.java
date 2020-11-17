package com.chinaunicom.rpc.entity;

public class Task<T> {

    private Integer id;

    private ServerThread serverThread;

    private T data;

    public Task(Integer id,T data,ServerThread serverThread){
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ServerThread getServerThread() {
        return serverThread;
    }

    public void setServerThread(ServerThread serverThread) {
        this.serverThread = serverThread;
    }
}
