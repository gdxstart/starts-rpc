package com.yupi.yurpc.config;

import com.yupi.yurpc.fault.retry.RetryStrategyKeys;
import com.yupi.yurpc.fault.tolerant.TolerantStrategyKeys;
import com.yupi.yurpc.loadbalancer.LoadBalancerKeys;
import com.yupi.yurpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {

    //名称
    private String name = "yu-rpc";

    //版本号
    private String version = "1.0";

    //主机
    private String serverHost = "localhost";


    //端口号
    private Integer serverPort = 8080;

    //模拟调用
    private boolean mock = false;

    //序列化器
    private String serializer = SerializerKeys.JDK;

    //注册中心配置
    private RegistryConfig  registryConfig= new RegistryConfig();

    // 负载均衡器
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    // 重试策略器
    private String retryStrategy = RetryStrategyKeys.NO;

    // 容错策略器
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;
}
