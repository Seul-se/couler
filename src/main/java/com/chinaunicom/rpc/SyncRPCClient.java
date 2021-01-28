package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.ClientSocketReader;
import com.chinaunicom.rpc.common.SocketWriter;
import com.chinaunicom.rpc.entity.ResultSet;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.RandomInt;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

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
        ResultSet<T> syncObj = new ResultSet<T>();
        try {
            socketReaders[i].getResultManager().putObj(id, syncObj);
            synchronized (syncObj) {
                socketWriters[i].write(serializer.serialize(req), id);
                syncObj.wait(timeout);
            }
        } catch (InterruptedException e) {
            Logger.error("线程异常唤醒:" + host + ":" + port, e);
        }

        T rsp = syncObj.getResult();
        if (rsp != null) {
            return rsp;
        } else {
            throw new IOException("获取返回结果超时 id:" + id);
        }
    }

}
