package com.yupi.yurpc.serializer;

import com.yupi.yurpc.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化器工厂：用于获取序列化器对象
 */
public class SerializerFactory {

    static {
        SpiLoader.load(Serializer.class);
    }
    /**
     * 序列化映射：用于实现单例
     */
    private static final Map<String,Serializer> KEY_SERIALIZER_MAP = new HashMap<String,Serializer>(){
        {
            put(SerializerKeys.JDK,new JdkSerializer());
            put(SerializerKeys.JSON,new JsonSerializer());
            put(SerializerKeys.KRYO,new KryoSerializer());
            put(SerializerKeys.HESSIAN,new HessianSerializer());
        }
    };

    // 默认的序列化器:jdk
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例对象
     * @param key
     * @return
     */
    public static Serializer getInstance(String key){
        return SpiLoader.getInstance(Serializer.class,key);
    }
}
