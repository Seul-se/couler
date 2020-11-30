package com.chinaunicom.rpc.intf;

public interface Serializer<T> {

    byte[] serialize(T obj);

    T deserialize(byte[] data);
}
