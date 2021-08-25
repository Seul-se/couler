package com.test.multibyte;

import com.chinaunicom.rpc.intf.SyncProcessor;
import com.chinaunicom.rpc.util.RandomInt;

import java.util.Random;

public class MyProcessorSync implements SyncProcessor<byte[][], byte[][]> {
    public byte[][] process(byte[][] req) {
        req[0] = ("server" + new String(req[0])).getBytes();
//        int rand = RandomInt.randomInt(1500);
//        try {
//            Thread.sleep(rand);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return req;
    }
}
