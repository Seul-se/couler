package com.test;

import com.chinaunicom.rpc.RPCClient;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {
        RPCClient<RequestPojo, ResponsePojo> client = new RPCClient<RequestPojo, ResponsePojo>("127.0.0.1",9004, RequestPojo.class, ResponsePojo.class,5);
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
