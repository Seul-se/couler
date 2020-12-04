package com.chinaunicom.rpc.utill;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RingBuffer<T> {

    private final int queueNum;

    private final AtomicInteger index = new AtomicInteger();

    private final AtomicReference<T>[] arrayQueue;

    private int cursor = 0 ;

    private final AtomicBoolean isWait = new AtomicBoolean(false);

    private boolean run = true;

    public RingBuffer(int queueNum){
        this.queueNum = queueNum;
        arrayQueue = new AtomicReference[queueNum];
        for(int i=0;i<arrayQueue.length;i++){
            arrayQueue[i] = new AtomicReference<T>();
        }
    }

    public void stop(){
        this.run = false;
    }

    public void offer(T obj){
        int index = this.index.getAndIncrement();
        while(run&!arrayQueue[index%queueNum].compareAndSet(null,obj)){
            Thread.yield();
        }
        if(isWait.compareAndSet(true,false)) {
            synchronized (isWait) {
                isWait.notify();
            }
        }
    }

    public T poll(){
        while(cursor == index.get()){
            try {
                synchronized (isWait) {
                    isWait.set(true);
                    isWait.wait();
                }
            } catch (InterruptedException e) {
                Logger.error("Socket写入线程异常中断", e);
            }
        }
        if(cursor >= queueNum){
            cursor -=queueNum;
            index.getAndAdd(-queueNum);
        }
        T datapackage = arrayQueue[cursor].get();
        while (datapackage == null){
            Thread.yield();
            datapackage = arrayQueue[cursor].get();
        }
        arrayQueue[cursor].set(null);
        cursor++;
        return datapackage;
    }
}
