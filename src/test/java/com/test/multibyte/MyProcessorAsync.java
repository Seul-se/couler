package com.test.multibyte;

import com.chinaunicom.rpc.entity.AsyncContext;
import com.chinaunicom.rpc.intf.AsyncProcessor;

public class MyProcessorAsync implements AsyncProcessor<byte[][], byte[][]> {

    public void process(byte[][] req, AsyncContext<byte[][]> asyncContext) {
        req[0] = ("server" + new String(req[0])).getBytes() ;
        asyncContext.setResult(req);
    }
}
