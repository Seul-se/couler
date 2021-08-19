package com.test.multibyte;

import com.chinaunicom.rpc.RPCServer;
import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.util.MultiByteSerializer;

import java.io.IOException;

public class SyncServer {
    public static void main(String[] args) {

        Serializer<byte[][]> serializer = new MultiByteSerializer();
        MyProcessorSync processor = new MyProcessorSync();
        RPCServer<byte[], byte[]> server = new RPCServer<byte[], byte[]>(9008, 300,processor);
        try {
            server.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
