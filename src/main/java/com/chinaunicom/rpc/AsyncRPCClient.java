package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.result.AbstractResultManager;
import com.chinaunicom.rpc.common.result.AsyncResultManager;
import com.chinaunicom.rpc.entity.ResultSet;
import com.chinaunicom.rpc.intf.ResultCallback;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.ByteSerializer;
import com.chinaunicom.rpc.util.ConnectionManager;
import com.chinaunicom.rpc.util.ThreadPool;

import java.io.IOException;

public class AsyncRPCClient<R,T> extends AbstractRPCClient<R,T> {


    public AsyncRPCClient(int connectNum, Serializer<R> serializer, Serializer<T> deserializer,int threadPoolSize) {
        super( connectNum, serializer, deserializer);
        this.threadPool = new ThreadPool(threadPoolSize);
        this.resultManager  = new AsyncResultManager<T>(threadPool,deserializer);
        connectionManager = new ConnectionManager(connectionNum,resultManager);
    }

    public AsyncRPCClient(int connectNum, Serializer serializer,int threadPoolSize) {
        this(connectNum,serializer,serializer,threadPoolSize);
    }

    public AsyncRPCClient( int connectNum, int threadPoolSize) {
        this(connectNum,new ByteSerializer(),threadPoolSize);
    }

    public void call(String host , int port, R req, ResultCallback<T> callback,int timeout) throws IOException {
        int id;
        ResultSet resultSet = new ResultSet(timeout);
        resultSet.setResult(callback);
        int i = 0;
        while(i < AbstractResultManager.RESULT_LENGTH) {
            i++;
            id = getId();
            resultSet.setId(id);
            if(resultManager.putObj(id, resultSet)) {
                connectionManager.send(host,port,serializer.serialize(req), id);
                return;
            }
            Thread.yield();
        }
        throw new IOException("发送队列已满");
    }



}
