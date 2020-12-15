package com.chinaunicom.rpc.intf;

public interface ResultCallback<T> {

    void onSuccess(T result);

    void onTimeout();
}
