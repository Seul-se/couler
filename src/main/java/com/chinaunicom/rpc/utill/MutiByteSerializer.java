package com.chinaunicom.rpc.utill;

import com.chinaunicom.rpc.intf.Serializer;

public class MutiByteSerializer implements Serializer<byte[][]> {

    public byte[] serialize(byte[][] obj) {
        int length = 4;
        for(int i=0;i<obj.length;i++){
            length +=4;
            length += obj[i].length;
        }
        byte[] content = new byte[length];
        byte[] len = Byte2Int.intToByteArray(obj.length);
        System.arraycopy(len ,0,content,0,4);
        int index = 4;
        for(int i=0;i<obj.length;i++){
            len = Byte2Int.intToByteArray(obj[i].length);
            System.arraycopy(len,0,content,index,4);
            index +=4;
            System.arraycopy(obj[i],0,content,index,obj[i].length);
            index +=obj[i].length;
        }
        return content;
    }

    public byte[][] deserialize(byte[] data) {
        byte[] len = new byte[4];
        System.arraycopy(data,0,len,0,4);
        int dataNum = Byte2Int.byteArrayToInt(len);
        int index = 4;
        int length;
        byte[][] result = new byte[dataNum][];
        for(int i=0;i<dataNum;i++){
            System.arraycopy(data,index,len,0,4);
            index +=4;
            length = Byte2Int.byteArrayToInt(len);
            byte[] one = new byte[length];
            System.arraycopy(data,index,one,0,length);
            index += length;
            result[i] = one;
        }
        return result;
    }
}
