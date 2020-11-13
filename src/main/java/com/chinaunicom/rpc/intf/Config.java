package com.chinaunicom.rpc.intf;

import io.protostuff.Schema;

public interface Config<T> {

     Schema<T> getSchema();
}
