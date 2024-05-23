package com.yupi.yurpc.fault.tolerant;

import com.yupi.yurpc.spi.SpiLoader;

/**
 * 容错策略工厂:用于获取容错策略对象
 */
public class TolerantStrategyFactory {
    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    // 默认容错策略
    private static final TolerantStrategy DEFAULT_TOLERANT_STRATEGY=new FailBackTolerantStrategy();

    // 获取实例对象
    public static TolerantStrategy getInstance(String key){
        return SpiLoader.getInstance(TolerantStrategy.class,key);
    }
}
