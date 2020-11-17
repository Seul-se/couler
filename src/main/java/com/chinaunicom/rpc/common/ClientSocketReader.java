package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.intf.Config;
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
        while (run&&socket.isConnected() && !socket.isClosed()) {
            try {
                readHead();
                Integer id = readId();
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
                resultManager.putResult(id, result);
            }catch (Exception e){
                Logger.error("Socket读取线程异常", e);
            }
        }
        close();
    }

    public void close(){
        super.close();
        try {
            resultManager.close();
        }catch (Exception e) {
            Logger.error("Socket读取线程关闭异常", e);
        }
    }

}
