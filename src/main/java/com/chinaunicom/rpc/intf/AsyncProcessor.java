package com.chinaunicom.rpc.intf;

import com.chinaunicom.rpc.entity.AsyncContext;

public interface AsyncProcessor<R,T> {

    /**
     * @param req
     * @param asyncContext
     */
    void process(R req, AsyncContext<T> asyncContext);
}
