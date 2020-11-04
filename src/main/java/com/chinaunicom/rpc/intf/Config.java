package com.chinaunicom.rpc.intf;

import com.chinaunicom.rpc.common.ResultManager;
import io.protostuff.Schema;

public interface Config<T> {

     Schema<T> getSchema();
}
