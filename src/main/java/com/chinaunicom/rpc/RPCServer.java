package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.*;
import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.Config;
import com.chinaunicom.rpc.intf.Processor;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.ProtostuffUtils;
import io.protostuff.Schema;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RPCServer<R,T> extends Thread implements Config<R> {

    private int port;
    private Schema<R> reqSchema;
    private Schema<T> rspSchema;

    public Schema<R> getSchema(){
        return reqSchema;
    }
    public Schema<T> getRspSchema(){
        return rspSchema;
    }

    ServerSocket server;


    private ProcessorThread<R,T> processorThread;

    public RPCServer(int port, Class<R> reqClz, Class<T> rspClz, int threadNum, Processor<R,T> processor){
        this.port = port;
        this.reqSchema = ProtostuffUtils.getSchema(reqClz);
        this.rspSchema = ProtostuffUtils.getSchema(rspClz);
        this.processorThread = new ProcessorThread<R,T>(processor,this,threadNum);

    }

    public void putTask(Task<R> t){
        processorThread.add(t);
    }
    public void open() throws IOException {
        server = new ServerSocket(port);
        Logger.info("RPC服务启动，PORT:" + port );
        this.start();
    }

    public void run(){
        while(true){
            Socket socket = null;
            try {
                socket = server.accept();
            } catch (IOException e) {
                Logger.error("建立连接异常" ,e );
            }
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
