package com.chinaunicom.rpc.common.socket;

import com.chinaunicom.rpc.RPCServer;
import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.Logger;

public class ServerSocketReader<R>  extends SocketReader<R>{

    RPCServer<R,Object> server;
    ServerThread serverThread;
    public ServerSocketReader(Serializer<R> deserializer, RPCServer server, ServerThread serverThread) {
        super(deserializer);
        this.server = server;
        this.serverThread = serverThread;
        this.deserializer = deserializer;

    }

    @Override
    public void run(){
        while (run&&socket.isConnected() && !socket.isClosed()) {
            try {
                readHead();
                int id = readId();
                if (id == -1) {
                    continue;
                }
                int length = readLength();
                if (length == -1) {
                    continue;
                }
                byte[] data = readData(length);
                if (data == null) {
                    continue;
                }
                R result = deserializer.deserialize(data);
                server.putTask(new Task<R>(id, result, serverThread));
            }catch (Exception e){
                Logger.error("Socket读取线程异常", e);
            }
        }
        Logger.info("连接关闭:" + socket.getRemoteSocketAddress().toString());
        close();
    }
}
