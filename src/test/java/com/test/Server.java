package com.test;

import com.chinaunicom.rpc.RPCServer;

import java.io.IOException;

public class Server {
    public static void main(String[] args) {

        RPCServer<RequestPojo, ResponsePojo> server = new RPCServer<RequestPojo, ResponsePojo>(9006, RequestPojo.class, ResponsePojo.class,200,new MyProcessor());
        try {
            server.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
