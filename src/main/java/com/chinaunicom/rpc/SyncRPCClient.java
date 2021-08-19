package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.result.SyncResultManager;
import com.chinaunicom.rpc.entity.ResultSet;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.ByteSerializer;
import com.chinaunicom.rpc.util.Logger;
import com.chinaunicom.rpc.util.RandomInt;

import java.io.IOException;

public class SyncRPCClient<R,T> extends AbstractRPCClient<R,T>  {


    public SyncRPCClient(String host, int port, int connectNum, Serializer<R> serializer, Serializer<T> deserializer){
        super(host,port,connectNum,serializer,deserializer);
        this.resultManager = new SyncResultManager<T>();
    }

    public SyncRPCClient(String host, int port, int connectNum, Serializer serializer){
        this(host,port,connectNum,serializer,serializer);
    }

    public SyncRPCClient(String host, int port, int connectNum){
        this(host,port,connectNum,new ByteSerializer());
    }

    public T call(R req,int timeout) throws IOException {
        if(availableSize==0){
            throw new IOException("没有可用连接");
        }
        int rand = RandomInt.randomInt(availableSize);
        int i = available.get(rand);
        ResultSet<T> syncObj = new ResultSet<T>(timeout);
        int id;
        boolean isWrite = false;
        while(true) {
            id = getId();
            syncObj.setId(id);
            try {
                if(resultManager.putObj(id, syncObj)){
                    isWrite = true;
                    synchronized (syncObj) {
                        socketWriters[i].write(serializer.serialize(req), id);
                        syncObj.wait(timeout);
                    }
                    break;
                }
            } catch (InterruptedException e) {
                Logger.error("线程异常唤醒:" + host + ":" + port, e);
                break;
            }finally {
                if(isWrite){
                    resultManager.removeObj(syncObj);
                }
            }
        }
        T rsp = syncObj.getResult();
        if (rsp != null) {
            return rsp;
        } else {
            throw new IOException("获取返回结果超时 id:" + id);
        }
    }

}
