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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class ProcessorThread<R,T> {

    private Queue<Task<R>> queue = new ConcurrentLinkedQueue<Task<R>>();


    private int size;
    private RunThread[] runThreads;

    public void add(Task<R> task){
        queue.offer(task);
        for(int i=0;i<size;i++) {
            if (runThreads[i].isWait.compareAndSet(true, false)) {
                LockSupport.unpark(runThreads[i]);
                break;
            }
        }
    }

    public ProcessorThread(SyncProcessor<R,T> processor, int size, Serializer<T> serializer){

        this.size = size;

        this.runThreads = new SyncRunThread[size];
        for(int i=0;i<size;i++){
            runThreads[i] = new SyncRunThread<R,T>(queue,processor,serializer);
        }
        for(Thread runThread: runThreads){
            runThread.start();
        }
    }

    public ProcessorThread(AsyncProcessor<R,T> processor, int size, Serializer<T> serializer){
        this.size = size;
        this.runThreads = new AsyncRunThread[size];
        for(int i=0;i<size;i++){
            runThreads[i] = new AsyncRunThread<R,T>(queue,processor,serializer);
        }
        for(Thread runThread: runThreads){
            runThread.start();
        }
    }

    private static abstract class RunThread extends Thread{
        public AtomicBoolean isWait = new AtomicBoolean(false);
    }

    public static class SyncRunThread<R,T> extends RunThread{

        private final Queue<Task<R>> queue;
        SyncProcessor<R,T> processor;
        private final Serializer<T> serializer;

        public SyncRunThread(Queue<Task<R>> queue, SyncProcessor<R,T> processor, Serializer<T> serializer){
            this.queue = queue;
            this.processor = processor;
            this.serializer = serializer;
        }

        public void run(){
            while (true){
                try {
                    Task<R> task = queue.poll();
                    if (task != null) {
                        T result = processor.process(task.getData());
                        byte[] resultData = serializer.serialize(result);
                        ServerThread serverThread = task.getServerThread();
                        serverThread.getSocketWriter().write(resultData, task.getId());
                    } else {
                        isWait.set(true);
                        LockSupport.park();
                    }
                }catch (Exception e){
                    Logger.error("处理线程异常：", e);
                }
            }
        }
    }

    public static class AsyncRunThread<R,T> extends RunThread{


        private final Queue<Task<R>> queue;
        AsyncProcessor<R, T> asyncProcessor;
        private final Serializer<T> serializer;

        private int yeildCount = 0;

        public AsyncRunThread(Queue<Task<R>> queue, AsyncProcessor<R, T> asyncProcessor, Serializer<T> serializer) {
            this.queue = queue;
            this.asyncProcessor = asyncProcessor;
            this.serializer = serializer;
        }

        public void run(){
            while (true){
                try {
                    Task<R> task = queue.poll();
                    if (task != null) {
                        asyncProcessor.process(task.getData(), new AsyncContext<T>(task, serializer));
                    } else {
                        isWait.set(true);
                        LockSupport.park();

                    }
                }catch (Exception e){
                    Logger.error("处理线程异常：", e);
                }
            }
        }
    }


}
