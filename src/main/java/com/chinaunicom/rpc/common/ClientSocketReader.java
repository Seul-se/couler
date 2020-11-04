package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.intf.Config;
import com.chinaunicom.rpc.intf.ReadProcess;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.ProtostuffUtils;

public class ClientSocketReader<T> extends SocketReader<T>{


    private ResultManager<T> resultManager = new ResultManager<T>();

    public ResultManager<T> getResultManager(){
        return  resultManager;
    }

    public ClientSocketReader(Config<T> config) {
        super(config);
        this.reconnect = true;
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
                T result = ProtostuffUtils.deserialize(data, config.getSchema());
//            config.getResultManager().putResult(id,result);
                resultManager.putResult(id, result);
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
