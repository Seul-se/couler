package com.chinaunicom.rpc.intf;

public interface RPCLogger {

    void info(String log);

    void info(String log,Throwable e);

    void error(String log);

    void error(String log,Throwable e);


}
