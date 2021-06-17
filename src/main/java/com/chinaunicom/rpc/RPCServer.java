package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.*;
import com.chinaunicom.rpc.common.server.AsyncProcessorThread;
import com.chinaunicom.rpc.common.server.ProcessorThread;
import com.chinaunicom.rpc.common.server.SyncProcessorThread;
import com.chinaunicom.rpc.common.socket.ServerSocketReader;
import com.chinaunicom.rpc.common.socket.SocketWriter;
import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.AsyncProcessor;
import com.chinaunicom.rpc.intf.SyncProcessor;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RPCServer<R,T> extends Thread  {

    private int port;

    private Serializer<R> deserializer;

    ServerSocket server;


    private ProcessorThread<R> processorThread;

    public RPCServer(int port, int threadNum, SyncProcessor<R,T> processor, Serializer serializer){
        this(port,threadNum,processor,serializer,serializer);
    }

    public RPCServer(int port, int threadNum, AsyncProcessor<R,T> asyncProcessor, Serializer serializer){
        this(port,threadNum,asyncProcessor,serializer,serializer);
    }

    public RPCServer(int port, int threadNum, SyncProcessor<R,T> processor, Serializer<T> serializer, Serializer<R> deserializer){
        this.port = port;
        this.processorThread = new SyncProcessorThread<R,T>(processor,threadNum,serializer);
        this.deserializer = deserializer;

    }
    public RPCServer(int port, int threadNum, AsyncProcessor<R,T> asyncProcessor, Serializer<T> serializer, Serializer<R> deserializer){
        this.port = port;
        this.processorThread = new AsyncProcessorThread<R,T>(asyncProcessor,threadNum,serializer);
        this.deserializer = deserializer;

    }

    public void putTask(Task<R> t){
        processorThread.add(t);
    }
    public void open() throws IOException {
        server = new ServerSocket(port);
        Logger.info("RPC服务启动，PORT:" + port );
        this.start();
    }

    @Override
    public void run(){
        while(true){
            Socket socket = null;
            try {
                socket = server.accept();
            } catch (IOException e) {
                Logger.error("建立连接异常" ,e );
            }
            Logger.info("建立连接:" + socket.getRemoteSocketAddress().toString() );
            SocketWriter<R> socketWriter = new SocketWriter<R>();
            socketWriter.init(socket);
            socketWriter.start();
            ServerThread serverThread = new ServerThread();
            ServerSocketReader<R> socketReader = new ServerSocketReader<R>(deserializer,this, serverThread);
            serverThread.setSocket(socket);
            serverThread.setSocketWriter(socketWriter);
            serverThread.setSocketReader(socketReader);
            socketReader.init(socket);
            socketReader.start();
        }
    }


}
