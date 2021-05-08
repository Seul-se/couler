package com.chinaunicom.rpc.intf;

public interface ResultCallback<T> {

    /**
     * @param result
     */
    void onSuccess(T result);

    /**
     *
     */
    void onTimeout();
}
