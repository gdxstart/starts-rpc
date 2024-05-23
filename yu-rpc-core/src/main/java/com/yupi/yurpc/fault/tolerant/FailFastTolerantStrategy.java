package com.yupi.yurpc.fault.tolerant;

import com.yupi.yurpc.model.RpcResponse;

import java.util.Map;

/**
 * 快速失败:容错策略(立即通知外部调用方)
 */
public class FailFastTolerantStrategy implements TolerantStrategy{
    /**
     * 容错
     * @param context 上下文,用于传递数据
     * @param e 异常
     * @return
     */
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {

        throw new RuntimeException("服务报错",e);
    }
}
