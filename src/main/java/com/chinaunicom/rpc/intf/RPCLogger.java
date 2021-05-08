package com.chinaunicom.rpc.intf;

public interface RPCLogger {

    /**
     * @param log
     */
    void info(String log);

    /**
     * @param log
     * @param e
     */
    void info(String log,Throwable e);

    /**
     * @param log
     */
    void error(String log);

    /**
     * @param log
     * @param e
     */
    void error(String log,Throwable e);


}
