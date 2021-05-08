package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.Logger;

public class ClientSocketReader<T> extends SocketReader<T>{


    private ResultManager<T> resultManager;

    public ResultManager<T> getResultManager(){
        return  resultManager;
    }

    public ClientSocketReader(Serializer<T> deserializer,boolean isAsync) {
        super(deserializer);
        if(isAsync){
            resultManager  = new AsyncResultManager<T>();
        }else{
            resultManager  = new ResultManager<T>();
        }
    }

    @Override
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
                T result = deserializer.deserialize(data);
                resultManager.putResult(id, result);
            }catch (Exception e){
                Logger.error("Socket读取线程异常", e);
            }
        }
        close();
    }

    @Override
    public void close(){
        super.close();
        try {
            resultManager.close();
        }catch (Exception e) {
            Logger.error("Socket读取线程关闭异常", e);
        }
    }

}
