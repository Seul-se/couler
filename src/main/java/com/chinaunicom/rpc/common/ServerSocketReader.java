package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.RPCServer;
import com.chinaunicom.rpc.entity.ServerThread;
import com.chinaunicom.rpc.entity.Task;
import com.chinaunicom.rpc.intf.Config;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.ProtostuffUtils;
import com.chinaunicom.rpc.utill.RandomInt;

public class ServerSocketReader<R>  extends SocketReader<R>{

    RPCServer<R,Object> server;
    ServerThread serverThread;
    public ServerSocketReader(Config<R> config, RPCServer server,ServerThread serverThread) {
        super(config);
        this.server = server;
        this.serverThread = serverThread;
    }

    public void run(){
        while (run) {
            if (socket.isConnected() && !socket.isClosed()) {
                readHead();
                Long id = readId();
                if (id == null) {
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
                R result = ProtostuffUtils.deserialize(data, config.getSchema());
                server.putTask(new Task<R>(id, result, serverThread));
            }else{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logger.error("Socket读取线程异常", e);
                }
            }
        }
    }
}
