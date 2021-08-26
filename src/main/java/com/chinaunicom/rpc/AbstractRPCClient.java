package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.result.AbstractResultManager;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.ConnectionManager;
import com.chinaunicom.rpc.util.Logger;
import com.chinaunicom.rpc.util.ThreadPool;

import java.util.concurrent.atomic.AtomicInteger;

public class AbstractRPCClient<R,T>  {

    protected int connectionNum;

    protected Serializer<R> serializer;

    protected Serializer<T> deserializer;

    protected ThreadPool threadPool;

    protected AbstractResultManager resultManager;

    protected ConnectionManager connectionManager;


    public AbstractRPCClient(int connectNum, Serializer<R> serializer, Serializer<T> deserializer){
        this.connectionNum = connectNum;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    AtomicInteger ids = new AtomicInteger(0);

    public static final Integer MAX_ID = Integer.MAX_VALUE / 2;

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
        try {
            resultManager.close();
        }catch (Exception e) {
            Logger.error("resultManager关闭异常", e);
        }
        connectionManager.close();
    }
}
