package com.test.multibyte;

import com.chinaunicom.rpc.intf.SyncProcessor;

public class MyProcessorSync implements SyncProcessor<byte[][], byte[][]> {
    public byte[][] process(byte[][] req) {
        req[0] = ("server" + new String(req[0])).getBytes();
        return req;
    }
}
