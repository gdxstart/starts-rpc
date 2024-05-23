package com.yupi.yurpc.protocol;

import com.yupi.yurpc.serializer.Serializer;
import com.yupi.yurpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 消息编码器:
 *  核心流程:依次向 Buffer 缓冲区中写入消息对象里的字段.
 */
public class ProtocolMessageEncoder {

    // 编码
    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException{
        if (protocolMessage == null || protocolMessage.getHeader() == null){
            return Buffer.buffer();
        }

        ProtocolMessage.Header header = protocolMessage.getHeader();

        // 依次向缓冲区写入字节
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());

        // 获取序列化器
        ProtocolMessageSerializeEnum serializeEnum = ProtocolMessageSerializeEnum.getEnumByKey(header.getSerializer());
        if (serializeEnum == null){
            throw new RuntimeException("序列化协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializeEnum.getValue());
        byte[] bytes = serializer.serialize(protocolMessage.getBody());

        // 写入 body 长度和数据
        buffer.appendInt(bytes.length);
        buffer.appendBytes(bytes);
        return buffer;
    }
}
