package com.chinaunicom.rpc.utill;

import com.chinaunicom.rpc.intf.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;


public class ProtostuffSerializer<T> implements Serializer<T> {

    /**
     * 避免每次序列化都重新申请Buffer空间
     */
//    private static LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    private Schema<T> schema;

    public ProtostuffSerializer(Class<T> tClass){
        this.schema = getSchema(tClass);
    }

    /**
     * 序列化方法，把指定对象序列化成字节数组
     *
     * @param obj
     * @param <T>
     * @return
     */
    private static <T> byte[] serialize(T obj,Schema<T> schema) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] data;
        try {
            data = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }

        return data;
    }

    /**
     * 反序列化方法，将字节数组反序列化成指定Class类型
     *
     * @param data
     * @param <T>
     * @return
     */
    private static <T> T deserialize(byte[] data, Schema<T> schema) {
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }


    public static <T> Schema<T> getSchema(Class<T> clazz) {
        return RuntimeSchema.getSchema(clazz);
    }


    public byte[] serialize(T obj) {
        return serialize(obj,schema);
    }

    public T deserialize(byte[] data) {
        return deserialize(data,schema);
    }
}