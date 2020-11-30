package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.RPCServer;
import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.Processor;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProcessorThread<R,T> {

    private ConcurrentLinkedQueue<Task<R>> queue = new ConcurrentLinkedQueue<Task<R>>();


    public void add(Task<R> task){
        this.queue.offer(task);
        synchronized (queue) {
            this.queue.notify();
        }
    }

    public ProcessorThread(Processor<R,T> processor, RPCServer<R,T> rpcServer,int size,Serializer<T> serializer){
        for(int i=0;i<size;i++){
            new RunThread<R,T>(queue,processor,rpcServer,serializer).start();
        }
    }
    public static class RunThread<R,T> extends Thread{

        private Queue<Task<R>> queue;
        Processor<R,T> processor;
        RPCServer rpcServer;
        private Serializer<T> serializer;

        public RunThread(Queue<Task<R>> queue,Processor<R,T> processor,RPCServer rpcServer,Serializer<T> serializer){
            this.queue = queue;
            this.processor = processor;
            this.rpcServer = rpcServer;
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
                    try {
                        synchronized (queue) {
                            this.queue.wait();
                        }
                    } catch (InterruptedException e) {
                        Logger.error("Process线程异常中断", e);
                    }
                }
            }
        }
    }


}
