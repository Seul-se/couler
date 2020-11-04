package com.chinaunicom.rpc.intf;

public interface ReadProcess<T>  {

    public void process(Long id,T data);
}
