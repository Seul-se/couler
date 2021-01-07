package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.entity.AsyncContext;
import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.AsyncProcessor;
import com.chinaunicom.rpc.intf.SyncProcessor;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.RandomInt;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public class ProcessorThread<R,T> {

//    private ConcurrentLinkedQueue<Task<R>> queue = new ConcurrentLinkedQueue<Task<R>>();

    private Queue<Task<R>>[] queues;

    private int size;
    private Thread[] runThreads;

    public void add(Task<R> task){
        int index = RandomInt.RandomInt(size);
        Queue<Task<R>> queue = queues[index];
        queue.offer(task);
        LockSupport.unpark(runThreads[index]);
    }

    public ProcessorThread(SyncProcessor<R,T> processor, int size, Serializer<T> serializer){
        this.size = size;

        queues = new ConcurrentLinkedQueue[size];
        this.runThreads = new RunThread[size];
        for(int i=0;i<size;i++){
            ConcurrentLinkedQueue<Task<R>> queue = new ConcurrentLinkedQueue<Task<R>>();
            queues[i] = queue;
            runThreads[i] = new RunThread<R,T>(queue,processor,serializer);
        }
        for(Thread runThread: runThreads){
            runThread.start();
        }
    }

    public ProcessorThread(AsyncProcessor<R,T> processor, int size, Serializer<T> serializer){
        this.size = size;
        queues = new ConcurrentLinkedQueue[size];
        this.runThreads = new AsyncRunThread[size];
        for(int i=0;i<size;i++){
            ConcurrentLinkedQueue<Task<R>> queue = new ConcurrentLinkedQueue<Task<R>>();
            queues[i] = queue;
            runThreads[i] = new AsyncRunThread<R,T>(queue,processor,serializer);
        }
        for(Thread runThread: runThreads){
            runThread.start();
        }
    }

    public static class RunThread<R,T> extends Thread{


        private final Queue<Task<R>> queue;
        SyncProcessor<R,T> processor;
        private final Serializer<T> serializer;

        public RunThread(Queue<Task<R>> queue, SyncProcessor<R,T> processor, Serializer<T> serializer){
            this.queue = queue;
            this.processor = processor;
            this.serializer = serializer;
        }

        public void run(){
            while (true){
                Task<R> task = queue.poll();
                if(task!=null){
                    T result = processor.process(task.getData());
                    byte[] resultData = serializer.serialize(result);
                    ServerThread serverThread = task.getServerThread();
                    serverThread.getSocketWriter().write(resultData,task.getId());
                }else{
                    LockSupport.park();
                }
            }
        }
    }

    public static class AsyncRunThread<R,T> extends Thread {


        private final Queue<Task<R>> queue;
        AsyncProcessor<R, T> asyncProcessor;
        private final Serializer<T> serializer;

        public AsyncRunThread(Queue<Task<R>> queue, AsyncProcessor<R, T> asyncProcessor, Serializer<T> serializer) {
            this.queue = queue;
            this.asyncProcessor = asyncProcessor;
            this.serializer = serializer;
        }

        public void run(){
            while (true){
                Task<R> task = queue.poll();
                if(task!=null){
                    asyncProcessor.process(task.getData(),new AsyncContext<T>(task,serializer));
                }else{
                    LockSupport.park();
//                    try {
//                        LockSupport.park();
//                        synchronized (waitSign) {
//                            waitSign.isWait = true;
//                            this.waitSign.wait();
//                        }
//                    } catch (InterruptedException e) {
//                        Logger.error("Process线程异常中断", e);
//                    }

                }
            }
        }
    }


}
