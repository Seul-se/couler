package com.chinaunicom.rpc.entity;

import com.chinaunicom.rpc.util.TimeUtil;

public class ResultSet<T> {

    private long startTime;

    private int id;

    private T result;

    private int timeout;

    public ResultSet(int id,int timeout){
        this.id = id;
        this.startTime = TimeUtil.getTime();
        this.timeout = timeout;
    }

    public ResultSet(int timeout){
        this.timeout = timeout;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.startTime = TimeUtil.getTime();
    }

    public boolean isTimeout(){
        if(startTime + timeout < TimeUtil.getTime()){
            return true;
        }
        return false;
    }
}
