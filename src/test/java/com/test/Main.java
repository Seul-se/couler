package com.test;

import com.chinaunicom.rpc.RPCServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        RPCServer server = new RPCServer(9004,Message.class,Message.class,200,new MyProcessor());
        try {
            server.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
