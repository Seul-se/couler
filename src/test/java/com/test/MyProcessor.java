package com.test;

import com.chinaunicom.rpc.intf.SyncProcessor;

public class MyProcessor implements SyncProcessor<RequestPojo, ResponsePojo> {
    public ResponsePojo process(RequestPojo req) {
        ResponsePojo rsp = new ResponsePojo();
        rsp.setCode("server" + req.getName());
        rsp.setRsp(req.getB());
        return rsp;
    }
}
