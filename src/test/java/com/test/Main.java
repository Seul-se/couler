package com.test;

import com.chinaunicom.rpc.util.MultiByteSerializer;

public class Main {

    static MultiByteSerializer multiByteSerializer = new MultiByteSerializer();

    public static void main(String[] args) {
        byte[][] bytes = new byte[4][];
        bytes[0] = "123".getBytes();
        bytes[1] = "456".getBytes();
        bytes[2] = "789".getBytes();
        bytes[3] = "abc".getBytes();
        byte[] result = multiByteSerializer.serialize(bytes);
        byte[][] result2= multiByteSerializer.deserialize(result);
        for(byte[] tmp: result2){
            System.out.println(new String(tmp));
        }
    }
}
