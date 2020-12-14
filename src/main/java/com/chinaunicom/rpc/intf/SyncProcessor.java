package com.chinaunicom.rpc.intf;

public interface SyncProcessor<R,T> {

    T process(R req);
}
