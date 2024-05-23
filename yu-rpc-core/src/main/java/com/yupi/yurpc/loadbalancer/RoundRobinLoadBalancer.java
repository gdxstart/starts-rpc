package com.yupi.yurpc.loadbalancer;

import com.yupi.yurpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器:使用JUC包下的 AtomicInteger 实现原子计数器,防止并发冲突问题
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    // 当前轮询的下标
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        // 没有,直接返回
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }

        // 只有一个,不用轮询
        int size = serviceMetaInfoList.size();
        if (size == 1) {
            return serviceMetaInfoList.get(0);
        }

        int index = currentIndex.getAndIncrement() % size;

        return serviceMetaInfoList.get(index);
    }
}
