package com.yupi.yurpc.fault.retry;

import com.yupi.yurpc.model.RpcResponse;
import org.junit.Test;

/**
 * 重试策略测试
 */
public class RetryStrategyTest {

    RetryStrategy retryStrategy1 = new NoRetryStrategy();

    RetryStrategy retryStrategy2 = new FixedIntervalRetryStrategy();

    @Test
    public void doRetry(){
        try {
            RpcResponse rpcResponse = retryStrategy2.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败");
            });
            System.out.println(rpcResponse);

        }catch (Exception e){
            System.out.println("重试多次失败");
            e.printStackTrace();
        }
    }
}
