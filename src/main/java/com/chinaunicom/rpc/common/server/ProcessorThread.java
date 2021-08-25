package com.chinaunicom.rpc.common.server;

import com.chinaunicom.rpc.entity.Task;

public interface ProcessorThread<R> {

    void add(Task task);
}
