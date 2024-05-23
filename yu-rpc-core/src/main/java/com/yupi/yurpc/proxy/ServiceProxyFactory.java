package com.yupi.yurpc.proxy;

import com.yupi.yurpc.RpcApplication;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂：用于创建代理对象
 */
public class ServiceProxyFactory  {
    /**
     * 根据服务类型获取到代理对象
     * @param serviceClass
     * @return
     * @param <T>
     */
    public static <T> T getProxy(Class<T> serviceClass){

        //如果有开启 Mock 服务模拟
        if (RpcApplication.getRpcConfig().isMock()){
            return getMockProxy(serviceClass);
        }
        /**
         * newProxyInstance:
         *  参数1:加载类
         *  参数2:Class[] interfaces
         *  参数3:代理类
         */
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy()
        );
    }

    /**
     * 根据服务类获取 Mock 代理对象
     * @param serviceClass
     * @return
     * @param <T>
     */
    public static <T> T getMockProxy(Class<T> serviceClass){
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy());
    }
}
