package com.chinaunicom.rpc.utill;

import com.chinaunicom.rpc.intf.Serializer;

public class MutiByteSerializerEncrypt implements Serializer<byte[][]> {

    private byte[] key;

    private MutiByteSerializer mutiByteSerializer = new MutiByteSerializer();

    public MutiByteSerializerEncrypt(byte[] key){
        this.key = key;
    }

    public byte[] serialize(byte[][] obj) {
        byte[] content = mutiByteSerializer.serialize(obj);
        content = encrypt(content,key);
        return content;
    }

    public byte[][] deserialize(byte[] data) {
        data = encrypt(data,key);
        byte[][] result = mutiByteSerializer.deserialize(data);
        return result;
    }

    private byte[] encrypt(byte[] data,byte[] key){
        int dataL = data.length;
        int keyL = key.length;
        for(int i=0;i<dataL;i++){
            data[i] = (byte)(data[i] ^ key[i%keyL]);
        }
        return data;
    }
}
