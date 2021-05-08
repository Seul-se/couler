package com.chinaunicom.rpc.intf;

public interface SyncProcessor<R,T> {

    /**
     * @param req
     * @return
     */
    T process(R req);
}
