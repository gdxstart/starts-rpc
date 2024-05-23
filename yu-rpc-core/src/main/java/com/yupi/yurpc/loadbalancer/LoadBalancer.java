package com.yupi.yurpc.loadbalancer;

import com.yupi.yurpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡器:消费者使用
 */
public interface LoadBalancer {

    ServiceMetaInfo select(Map<String ,Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}
