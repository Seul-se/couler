package com.chinaunicom.rpc.common.socket;

import com.chinaunicom.rpc.common.result.AbstractResultManager;
import com.chinaunicom.rpc.util.Logger;

public class ClientSocketReader extends SocketReader{


    private AbstractResultManager resultManager;

    public AbstractResultManager getResultManager(){
        return  resultManager;
    }

    public ClientSocketReader(AbstractResultManager resultManager) {
        this.resultManager = resultManager;
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
                resultManager.putResult(id, data);
            }catch (Exception e){
                Logger.error("Socket读取线程异常", e);
            }
        }
        close();
    }

    @Override
    public void close(){
        super.close();

    }

}
