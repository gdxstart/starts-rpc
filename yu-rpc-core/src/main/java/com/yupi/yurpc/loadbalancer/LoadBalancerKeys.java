package com.yupi.yurpc.loadbalancer;

public interface LoadBalancerKeys {
    // 轮询
    String ROUND_ROBIN = "roundRobin";

    String RANDOM = "random";

    String CONSISTENT_HASH = "consistentHash";
}
