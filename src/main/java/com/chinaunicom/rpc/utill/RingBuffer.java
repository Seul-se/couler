package com.chinaunicom.rpc.utill;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RingBuffer<T> {

    private final int queueNum;

    private final AtomicInteger index = new AtomicInteger();

    private final AtomicReference<T>[] arrayQueue;

    private int cursor = 0 ;


    public RingBuffer(int queueNum){
        this.queueNum = queueNum;
        arrayQueue = new AtomicReference[queueNum];
        for(int i=0;i<arrayQueue.length;i++){
            arrayQueue[i] = new AtomicReference<T>();
        }
    }

    public void offer(T obj) throws IOException {
        if(obj == null){
            throw new RuntimeException("Object is null");
        }
        int index = this.index.getAndIncrement();
        if((index-queueNum)>=cursor||!arrayQueue[index%queueNum].compareAndSet(null,obj)){
            throw new IOException("Queue is full");
        }
    }

    public T poll(){
        if(cursor >= index.get()){
           return null;
        }
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
