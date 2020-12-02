package com.chinaunicom.rpc;

import com.chinaunicom.rpc.intf.ResultCallback;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.RandomInt;

import java.io.IOException;

public class AsyncRPCClient<R,T> extends AbstractRPCClient<R,T> {


    public AsyncRPCClient(String host, int port, int connectNum, Serializer<R> serializer, Serializer<T> deserializer) {
        super(host, port, connectNum, serializer, deserializer);
        this.isAsync = true;
    }

    public void call(R req, ResultCallback<T> callback) throws IOException {
        if(aviableSize==0){
            throw new IOException("没有可用连接");
        }
        int rand = RandomInt.RandomInt(aviableSize);
        int i = aviable.get(rand);
        int id = getId();
        socketReaders[i].getResultManager().putObj(id,callback);
        socketWriters[i].write(serializer.serialize(req),id);
    }
}