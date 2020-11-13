package com.chinaunicom.rpc.intf;

public interface Processor<R,T> {

    T process(R req);
}
