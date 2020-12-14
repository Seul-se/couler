package com.chinaunicom.rpc.entity;

import com.chinaunicom.rpc.intf.Serializer;

public class AsyncContext<T> {

    private Serializer<T> serializer;

    private Task task;

    public AsyncContext(Task task, Serializer<T> serializer){
        this.serializer = serializer;
        this.task = task;
    }

    public void setResult(T result){
        byte[] resultData = serializer.serialize(result);
        ServerThread serverThread = task.getServerThread();
        serverThread.getSocketWriter().write(resultData,task.getId());
    }
}
