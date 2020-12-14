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

public class ProcessorThread<R,T> {

//    private ConcurrentLinkedQueue<Task<R>> queue = new ConcurrentLinkedQueue<Task<R>>();

    private Queue<Task<R>>[] queues;

    public WaitSign[] waitSigns;

    private int size;

    public void add(Task<R> task){
        int index = RandomInt.RandomInt(size);
        Queue<Task<R>> queue = queues[index];
        queue.offer(task);
        WaitSign waitSign = waitSigns[index];
        if(waitSign.isWait) {
            synchronized (waitSign) {
                waitSign.notify();
                waitSigns[index].isWait = false;
            }
        }
    }

    public ProcessorThread(SyncProcessor<R,T> processor, int size, Serializer<T> serializer){
        this.size = size;
        waitSigns = new WaitSign[size];
        queues = new ConcurrentLinkedQueue[size];
        RunThread<R,T>[] runThreads = new RunThread[size];
        for(int i=0;i<size;i++){
            ConcurrentLinkedQueue<Task<R>> queue = new ConcurrentLinkedQueue<Task<R>>();
            queues[i] = queue;
            waitSigns[i] = new WaitSign();
            runThreads[i] = new RunThread<R,T>(queue,processor,serializer,waitSigns[i],queues,i);
        }
        for(RunThread runThread: runThreads){
            runThread.start();
        }
    }

    public ProcessorThread(AsyncProcessor<R,T> processor, int size, Serializer<T> serializer){
        this.size = size;
        waitSigns = new WaitSign[size];
        queues = new ConcurrentLinkedQueue[size];
        AsyncRunThread<R,T>[] runThreads = new AsyncRunThread[size];
        for(int i=0;i<size;i++){
            ConcurrentLinkedQueue<Task<R>> queue = new ConcurrentLinkedQueue<Task<R>>();
            queues[i] = queue;
            waitSigns[i] = new WaitSign();
            runThreads[i] = new AsyncRunThread<R,T>(queue,processor,serializer,waitSigns[i],queues,i);
        }
        for(AsyncRunThread runThread: runThreads){
            runThread.start();
        }
    }

    private static class WaitSign{
        public boolean isWait = false;
    }

    public static class RunThread<R,T> extends Thread{

        private Queue<Task<R>>[] queues;
        private int id;

        private final Queue<Task<R>> queue;
        SyncProcessor<R,T> processor;
        private final Serializer<T> serializer;
        public WaitSign waitSign;

        public RunThread(Queue<Task<R>> queue, SyncProcessor<R,T> processor, Serializer<T> serializer, WaitSign waitSign, Queue<Task<R>>[] queues, int id){
            this.queue = queue;
            this.processor = processor;
            this.serializer = serializer;
            this.waitSign = waitSign;
            this.queues = queues;
            this.id = id;
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
                    int i= id;
                    int end = id==0?queues.length-1:id-1;
                    while(i!=end){
                        i++;
                        if(i>=queues.length-1){
                            i = 0;
                        }
                        task = queues[i].poll();
                        if(task!= null){
                            T result = processor.process(task.getData());
                            byte[] resultData = serializer.serialize(result);
                            ServerThread serverThread = task.getServerThread();
                            serverThread.getSocketWriter().write(resultData,task.getId());
                            break;
                        }
                    }
                    if(task == null) {
                        try {
                            synchronized (waitSign) {
                                waitSign.isWait = true;
                                this.waitSign.wait();
                            }
                        } catch (InterruptedException e) {
                            Logger.error("Process线程异常中断", e);
                        }
                    }
                }
            }
        }
    }

    public static class AsyncRunThread<R,T> extends Thread {

        private Queue<Task<R>>[] queues;
        private int id;

        private final Queue<Task<R>> queue;
        AsyncProcessor<R, T> asyncProcessor;
        private final Serializer<T> serializer;
        public WaitSign waitSign;

        public AsyncRunThread(Queue<Task<R>> queue, AsyncProcessor<R, T> asyncProcessor, Serializer<T> serializer,WaitSign waitSign,Queue<Task<R>>[] queues,int id) {
            this.queue = queue;
            this.asyncProcessor = asyncProcessor;
            this.serializer = serializer;
            this.waitSign = waitSign;
            this.queues = queues;
            this.id = id;
        }

        public void run(){
            while (true){
                Task<R> task = queue.poll();
                if(task!=null){
                    asyncProcessor.process(task.getData(),new AsyncContext<T>(task,serializer));
                }else{
                    int i= id;
                    int end = id==0?queues.length-1:id-1;
                    while(i!=end){
                        i++;
                        if(i>=queues.length-1){
                            i = 0;
                        }
                        task = queues[i].poll();
                        if(task!= null){
                            asyncProcessor.process(task.getData(),new AsyncContext<T>(task,serializer));
                            break;
                        }
                    }
                    if(task == null) {
                        try {
                            synchronized (waitSign) {
                                waitSign.isWait = true;
                                this.waitSign.wait();
                            }
                        } catch (InterruptedException e) {
                            Logger.error("Process线程异常中断", e);
                        }
                    }
                }
            }
        }
    }


}
