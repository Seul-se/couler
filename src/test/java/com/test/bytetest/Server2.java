package com.test.bytetest;

import com.chinaunicom.rpc.RPCServer;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.MutiByteSerializer;
import com.chinaunicom.rpc.utill.MutiByteSerializerEncrypt;

import java.io.IOException;

public class Server2 {
    public static void main(String[] args) {

        Serializer<byte[][]> serializer = new MutiByteSerializerEncrypt("abc".getBytes());
        MyProcessor2 processor = new MyProcessor2();
        RPCServer<byte[][], byte[][]> server = new RPCServer<byte[][], byte[][]>(9008, 300,processor,serializer,serializer);
        try {
            server.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
