package com.chinaunicom.rpc.common.socket;

import com.chinaunicom.rpc.common.result.AbstractResultManager;
import com.chinaunicom.rpc.common.result.AsyncResultManager;
import com.chinaunicom.rpc.common.result.SyncResultManager;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.Logger;
import com.chinaunicom.rpc.util.ThreadPool;

public class ClientSocketReader<T> extends SocketReader<T>{


    private AbstractResultManager<T> resultManager;

    public AbstractResultManager<T> getResultManager(){
        return  resultManager;
    }

    public ClientSocketReader(Serializer<T> deserializer, ThreadPool threadPool) {
        super(deserializer);
        if(threadPool!=null){
            resultManager  = new AsyncResultManager<T>(threadPool);
        }else{
            resultManager  = new SyncResultManager<T>();
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
