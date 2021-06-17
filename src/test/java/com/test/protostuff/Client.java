package com.test.protostuff;

import com.chinaunicom.rpc.SyncRPCClient;
import com.chinaunicom.rpc.util.ProtostuffSerializer;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {

        ProtostuffSerializer<RequestPojo> protostuffSerializer = new ProtostuffSerializer<RequestPojo>(RequestPojo.class);
        ProtostuffSerializer<ResponsePojo> protostuffDeSerializer = new ProtostuffSerializer<ResponsePojo>(ResponsePojo.class);
        SyncRPCClient<RequestPojo, ResponsePojo> client = new SyncRPCClient<RequestPojo, ResponsePojo>("127.0.0.1",9008, 5,protostuffSerializer,protostuffDeSerializer);
        client.open();
        RequestPojo message = new RequestPojo();
        message.setName("123");
        message.setId(100);
        message.setB("456".getBytes());
        try {
            System.out.println(client.call(message,10000).getCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

