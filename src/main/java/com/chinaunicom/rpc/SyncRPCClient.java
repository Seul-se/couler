package com.chinaunicom.rpc;

import com.chinaunicom.rpc.common.result.SyncResultManager;
import com.chinaunicom.rpc.entity.ResultSet;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.ByteSerializer;
import com.chinaunicom.rpc.util.ConnectionManager;
import com.chinaunicom.rpc.util.Logger;

import java.io.IOException;

public class SyncRPCClient<R,T> extends AbstractRPCClient<R,T>  {


    public SyncRPCClient( int connectNum, Serializer<R> serializer, Serializer<T> deserializer){
        super(connectNum,serializer,deserializer);
        this.resultManager = new SyncResultManager();
        this.connectionManager = new ConnectionManager(connectionNum,resultManager);
    }

    public SyncRPCClient( int connectNum, Serializer serializer){
        this(connectNum,serializer,serializer);
    }

    public SyncRPCClient( int connectNum){
        this(connectNum,new ByteSerializer());
    }

    public T call(String host, int port ,R req,int timeout) throws IOException {
        ResultSet<byte[]> syncObj = new ResultSet<byte[]>(timeout);
        int id;
        boolean isWrite = false;
        while(true) {
            id = getId();
            syncObj.setId(id);
            try {
                if(resultManager.putObj(id, syncObj)){
                    isWrite = true;
                    synchronized (syncObj) {
                        connectionManager.send(host,port,serializer.serialize(req), id);
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
        byte[] rsp = syncObj.getResult();
        if (rsp != null) {
            return deserializer.deserialize(rsp);
        } else {
            throw new IOException("获取返回结果超时 id:" + id);
        }
    }

}
