package com.test;

import com.chinaunicom.rpc.RPCServer;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.ProtostuffSerializer;

import java.io.IOException;

public class Server {
    public static void main(String[] args) {

        ProtostuffSerializer<RequestPojo> protostuffDeSerializer = new ProtostuffSerializer<RequestPojo>(RequestPojo.class);
        ProtostuffSerializer<ResponsePojo> protostuffSerializer = new ProtostuffSerializer<ResponsePojo>(ResponsePojo.class);

        RPCServer<RequestPojo, ResponsePojo> server = new RPCServer<RequestPojo, ResponsePojo>(9008, 3000,new MyProcessor(),protostuffSerializer,protostuffDeSerializer);
        try {
            server.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
