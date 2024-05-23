package com.yupi.yurpc.serializer;

import java.io.IOException;

/**
 * 序列化接口
 */
public interface Serializer {
    //序列化：将java对象转为可传输的字节数组。
    <T> byte[] serialize(T object) throws IOException;

    //反序列化：将字节数组转为java对象。
    <T> T deserialize(byte[] bytes,Class<T> type) throws IOException;
}
