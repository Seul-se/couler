package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.RPCServer;
import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.Processor;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.ProtostuffUtils;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProcessorThread<R,T> extends Thread {

    private ConcurrentLinkedQueue<Task<R>> queue = new ConcurrentLinkedQueue<Task<R>>();
    private Processor<R,T> processor;
    private RPCServer rpcServer;

    public void add(Task<R> task){
        this.queue.offer(task);
        synchronized (queue) {
            this.queue.notify();
        }
    }

    public ProcessorThread(Processor<R,T> processor, RPCServer<R,T> rpcServer){
        this.processor = processor;
        this.rpcServer = rpcServer;
    }

    public void run(){
        while (true){
            Task<R> task = queue.poll();
            if(task!=null){
                T result = processor.process(task.getData());
                byte[] resultData = ProtostuffUtils.serialize(result,rpcServer.getRspSchema());
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
