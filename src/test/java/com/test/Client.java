package com.test;

import com.chinaunicom.rpc.RPCClient;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {
        RPCClient<String,String> client = new RPCClient<String,String>("127.0.0.1",9004,String.class,String.class,5);
        client.open();
        try {
            System.out.println(client.call("123",10000));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
