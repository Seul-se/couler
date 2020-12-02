package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.ClientSocketReader;
import com.chinaunicom.rpc.common.SocketWriter;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.RandomInt;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncRPCClient<R,T> extends AbstractRPCClient<R,T>  {


    protected boolean isAsync;


    public SyncRPCClient(String host, int port, int connectNum, Serializer<R> serializer, Serializer<T> deserializer){
        super(host,port,connectNum,serializer,deserializer);
        this.isAsync = false;
    }


    public T call(R req,int timeout) throws IOException {
        if(aviableSize==0){
            throw new IOException("没有可用连接");
        }
        int rand = RandomInt.RandomInt(aviableSize);
        int i = aviable.get(rand);
        int id = getId();
        Object syncObj = objContainer.get();
        if(syncObj == null ){
            syncObj = new Object();
            objContainer.set(syncObj);
        }
        socketReaders[i].getResultManager().putObj(id,syncObj);
        try {
            synchronized (syncObj) {
                socketWriters[i].write(serializer.serialize(req),id);
                syncObj.wait(timeout);
            }
        } catch (InterruptedException e) {
            Logger.error("线程异常唤醒:" + host + ":" + port ,e);
        }
        T rsp = socketReaders[i].getResultManager().getResult(id);
        if(rsp!=null){
            return rsp;
        }else{
            throw new IOException("获取返回结果超时 id:" + id);
        }
    }

}
