package com.yupi.yurpc.fault.retry;

import com.yupi.yurpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 不重试:重试策略
 */
public class NoRetryStrategy implements RetryStrategy{
    /**
     * 重试
     * @param callable 代表一个任务
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        // 任务唤醒
        return callable.call();
    }
}
