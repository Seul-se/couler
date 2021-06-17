package com.chinaunicom.rpc.util;


import java.util.concurrent.*;

public class ThreadPool {

    ThreadPoolExecutor threadPoolExecutor;

    public ThreadPool(int poolSize){
        threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.SECONDS,new ArrayBlockingQueue(3000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void submit(Runnable runnable){
        threadPoolExecutor.submit(runnable);
    }
}
