package com.chinaunicom.rpc.intf;

import com.chinaunicom.rpc.entity.AsyncContext;

public interface AsyncProcessor<R,T> {

    void process(R req, AsyncContext<T> asyncContext);
}
