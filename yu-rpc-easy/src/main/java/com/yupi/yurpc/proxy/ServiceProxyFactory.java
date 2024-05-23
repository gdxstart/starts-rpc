package com.yupi.yurpc.proxy;

import java.lang.reflect.Proxy;


public class ServiceProxyFactory  {
    /**
     * 根据服务类型获取到代理对象
     * @param serviceClass
     * @return
     * @param <T>
     */
    public static <T> T getProxy(Class<T> serviceClass){
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
}
