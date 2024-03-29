package com.chinaunicom.rpc.util;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RingBuffer<T> {

    private final int queueNum;

    private final int computeNum;

    private final AtomicInteger index = new AtomicInteger();

    private final AtomicReference<T>[] arrayQueue;

    private int cursor = 0 ;

    private AtomicInteger size = new AtomicInteger();


    public RingBuffer(int queueNum){
        this.queueNum = queueNum;
        this.computeNum = queueNum - 1;
        arrayQueue = new AtomicReference[queueNum];
        for(int i=0;i<arrayQueue.length;i++){
            arrayQueue[i] = new AtomicReference<T>();
        }
    }

    public void offer(T obj) throws IOException {
        if(obj == null){
            throw new RuntimeException("Object is null");
        }
        while (size.get() < queueNum){
            int index = this.index.getAndIncrement();
            if(arrayQueue[index & queueNum].compareAndSet(null,obj)){
                size.incrementAndGet();
                return;
            }
        }
    }

    public T poll(){
        if(cursor >= queueNum){
            cursor -=queueNum;
            index.getAndAdd(-queueNum);
        }
        T datapackage = arrayQueue[cursor].get();
        if (datapackage == null){
            return null;
        }
        arrayQueue[cursor].set(null);
        cursor++;
        return datapackage;
    }
}
