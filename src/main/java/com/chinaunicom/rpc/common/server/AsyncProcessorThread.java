package com.chinaunicom.rpc.common.server;

import com.chinaunicom.rpc.entity.AsyncContext;
import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.AsyncProcessor;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.intf.SyncProcessor;
import com.chinaunicom.rpc.util.ThreadPool;

public class AsyncProcessorThread<R,T> implements ProcessorThread<R> {


    AsyncProcessor<R,T> processor;

    Serializer<T> serializer;

    ThreadPool threadPool;

    public AsyncProcessorThread(AsyncProcessor<R,T> processor, int poolSize, Serializer<T> serializer){
        this.processor = processor;
        this.threadPool = new ThreadPool(poolSize);
        this.serializer = serializer;
    }


    @Override
    public void add(final Task<R> task) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                processor.process(task.getData(), new AsyncContext<T>(task, serializer));
            }
        };
        threadPool.submit(runnable);
    }
}
