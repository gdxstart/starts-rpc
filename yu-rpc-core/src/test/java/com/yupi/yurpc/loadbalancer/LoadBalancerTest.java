package com.yupi.yurpc.loadbalancer;

import com.yupi.yurpc.model.ServiceMetaInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡器测试
 */
public class LoadBalancerTest {

    final LoadBalancer loadBalancer = new ConsistentHashLoadBalancer();

    @Test
    public void select(){

        // 请求参数
        Map<String,Object> requestParams = new HashMap<>();
        requestParams.put("methodName","apple");

        // 服务列表
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);

        ServiceMetaInfo serviceMetaInfo2 = new ServiceMetaInfo();
        serviceMetaInfo2.setServiceName("myService");
        serviceMetaInfo2.setServiceVersion("1.0");
        serviceMetaInfo2.setServiceHost("yupi.icu");
        serviceMetaInfo2.setServicePort(80);

        List<ServiceMetaInfo> serviceMetaInfoList = Arrays.asList(serviceMetaInfo, serviceMetaInfo2);

        // 连续调用三次
        ServiceMetaInfo serviceMetaInfo1 = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println(serviceMetaInfo1);
        Assert.assertNotNull(serviceMetaInfo1);
        serviceMetaInfo1 = loadBalancer.select(requestParams,serviceMetaInfoList);
        System.out.println(serviceMetaInfo1);
        Assert.assertNotNull(serviceMetaInfo1);
        serviceMetaInfo1 = loadBalancer.select(requestParams,serviceMetaInfoList);
        System.out.println(serviceMetaInfo1);
        Assert.assertNotNull(serviceMetaInfo1);
    }
}
