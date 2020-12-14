package com.chinaunicom.rpc.intf;

public interface ResultCallback<T> {

    void call(T result);

    void onTimeout();
}
