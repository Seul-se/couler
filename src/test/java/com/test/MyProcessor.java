package com.test;

import com.chinaunicom.rpc.intf.Processor;

public class MyProcessor implements Processor<Message,Message> {
    public Message process(Message req) {
        req.setId(req.getId() +10);
        req.setName("server" + req.getName());
        return req;
    }
}
