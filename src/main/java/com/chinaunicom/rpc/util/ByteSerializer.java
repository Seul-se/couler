package com.chinaunicom.rpc.util;

import com.chinaunicom.rpc.intf.Serializer;

public class ByteSerializer implements Serializer<byte[]> {

    @Override
    public byte[] serialize(byte[] obj) {
        return obj;
    }

    @Override
    public byte[] deserialize(byte[] data) {
        return data;
    }
}
