package com.chinaunicom.rpc.entity;

public class Task<T> {

    private Long id;

    private ServerThread serverThread;

    private T data;

    public Task(Long id,T data,ServerThread serverThread){
        this.id = id ;
        this.data = data;
        this.serverThread = serverThread;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
