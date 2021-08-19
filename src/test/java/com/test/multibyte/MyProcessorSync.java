package com.test.multibyte;

import com.chinaunicom.rpc.intf.SyncProcessor;

public class MyProcessorSync implements SyncProcessor<byte[], byte[]> {
    public byte[] process(byte[] req) {
        req = ("server" + new String(req)).getBytes();
        return req;
    }
}
