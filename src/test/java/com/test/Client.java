package com.test;

import com.chinaunicom.rpc.RPCClient;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {
        RPCClient<Message,Message> client = new RPCClient<Message,Message>("127.0.0.1",9004,Message.class,Message.class,5);
        client.open();
        Message message = new Message();
        message.setName("123");
        message.setId(100);
        message.setB("456".getBytes());
        try {
            System.out.println(client.call(message,10000).getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
