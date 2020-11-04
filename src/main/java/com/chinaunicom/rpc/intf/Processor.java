package com.chinaunicom.rpc.intf;

public interface Processor<R,T> {

    public T process(R req);
}
