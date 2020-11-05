package com.test;

import com.chinaunicom.rpc.intf.Processor;

public class MyProcessor implements Processor<RequestPojo, ResponsePojo> {
    public ResponsePojo process(RequestPojo req) {
        ResponsePojo rsp = new ResponsePojo();
        rsp.setCode("server" + req.getName());
        rsp.setRsp(req.getB());
        return rsp;
    }
}
