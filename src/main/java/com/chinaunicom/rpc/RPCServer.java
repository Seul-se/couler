package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.*;
import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.Config;
import com.chinaunicom.rpc.intf.Processor;
import com.chinaunicom.rpc.intf.ReadProcess;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.ProtostuffUtils;
import io.protostuff.Schema;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class RPCServer<R,T> implements Config<R> {

    private int port;
    private Schema<R> reqSchema;
    private Schema<T> rspSchema;
    private int threadNum;

    public Schema<R> getSchema(){
        return reqSchema;
    }
    public Schema<T> getRspSchema(){
        return rspSchema;
    }



    private ProcessorThread<R,T>[] processorThreads;

    public RPCServer(int port, Class<R> reqClz, Class<T> rspClz, int threadNum, Processor<R,T> processor){
        this.port = port;
        this.reqSchema = ProtostuffUtils.getSchema(reqClz);
        this.rspSchema = ProtostuffUtils.getSchema(rspClz);
        this.threadNum = threadNum;
        this.processorThreads = new ProcessorThread[threadNum];
        for(int i=0;i<processorThreads.length;i++){
            processorThreads[i] = new ProcessorThread<R, T>(processor,this);
            processorThreads[i].start();
        }
    }
    private  Random b=new Random();

    public void putTask(Task<R> t){
        processorThreads[b.nextInt(threadNum)].add(t);
    }
    public void open() throws IOException {
        ServerSocket server = new ServerSocket(port);
        Logger.info("RPC服务启动，PORT:" + port );
        while(true){
            Socket socket = server.accept();
            Logger.info("建立连接:" + socket.getRemoteSocketAddress().toString() );
            SocketWriter<R> socketWriter = new SocketWriter<R>(this);
            socketWriter.init(socket);
            socketWriter.start();
            ServerThread serverThread = new ServerThread();
            ServerSocketReader<R> socketReader = new ServerSocketReader<R>(this,this, serverThread);
            serverThread.setSocket(socket);
            serverThread.setSocketWriter(socketWriter);
            serverThread.setSocketReader(socketReader);
            socketReader.init(socket);
            socketReader.start();


        }


    }


}
