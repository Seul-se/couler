package com.chinaunicom.rpc.common.server;

import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.intf.SyncProcessor;
import com.chinaunicom.rpc.util.ThreadPool;

public class SyncProcessorThread<R,T> implements ProcessorThread<R> {


    SyncProcessor<R,T> processor;

    Serializer<T> serializer;

    ThreadPool threadPool;

    Serializer<R> deserializer;

    public SyncProcessorThread(SyncProcessor<R,T> processor, int poolSize, Serializer<T> serializer,Serializer<R> deserializer){
        this.processor = processor;
        this.threadPool = new ThreadPool(poolSize);
        this.serializer = serializer;
        this.deserializer = deserializer;
    }


    @Override
    public void add(final Task task) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                T result = processor.process(deserializer.deserialize(task.getData()));
                byte[] resultData = serializer.serialize(result);
                ServerThread serverThread = task.getServerThread();
                serverThread.getSocketWriter().write(resultData, task.getId());
            }
        };
        threadPool.submit(runnable);
    }
}
