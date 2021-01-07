package com.chinaunicom.rpc.entity;

public class ResultSet<T> {

    private T result;

    private Thread t;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Thread getT() {
        return t;
    }

    public void setT(Thread t) {
        this.t = t;
    }
}
