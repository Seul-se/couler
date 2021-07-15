package com.chinaunicom.rpc;

import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.intf.ResultCallback;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.ByteSerializer;
import com.chinaunicom.rpc.util.Logger;
import com.chinaunicom.rpc.util.RandomInt;
import com.chinaunicom.rpc.util.ThreadPool;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public class AsyncRPCClient<R,T> extends AbstractRPCClient<R,T> {


    public AsyncRPCClient(String host, int port, int connectNum, Serializer<R> serializer, Serializer<T> deserializer,int threadPoolSize) {
        super(host, port, connectNum, serializer, deserializer);
        this.threadPool = new ThreadPool(threadPoolSize);

    }

    public AsyncRPCClient(String host, int port, int connectNum, Serializer serializer,int threadPoolSize) {
        this(host,port,connectNum,serializer,serializer,threadPoolSize);
    }

    public AsyncRPCClient(String host, int port, int connectNum, int threadPoolSize) {
        this(host,port,connectNum,new ByteSerializer(),threadPoolSize);
    }

    public void call(R req, ResultCallback<T> callback) throws IOException {
        if(availableSize==0){
            throw new IOException("没有可用连接");
        }
        int rand = RandomInt.randomInt(availableSize);
        int i = available.get(rand);
        int id = getId();
        socketReaders[i].getResultManager().putObj(id,callback);
        socketWriters[i].write(serializer.serialize(req),id);
    }



}
