package com.chinaunicom.rpc.intf;

public interface Serializer<T> {

    /**
     * @param obj
     * @return
     */
    byte[] serialize(T obj);

    /**
     * @param data
     * @return
     */
    T deserialize(byte[] data);
}
