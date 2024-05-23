package com.yupi.yurpc.proxy;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock 服务代理 （JDK 动态代理）
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param method the {@code Method} instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the {@code Method} object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or {@code null} if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();
        log.info("mock invoke {}",method.getName());
        return getDefaultObject(returnType);
    }

    /**
     * 生成指定类型的默认值对象 TODO（可完善默认值逻辑）
     * @param type
     * @return
     */
    private Object getDefaultObject(Class<?> type) {
        //基本类型
        if (type.isPrimitive()){
            if (type == boolean.class){
                return false;
            }else if (type == short.class){
                return (short)0;
            }else if (type == int.class){
                return 0;
            }else if (type == long.class){
                return 0L;
            }
        }

        //对象类型
        return null;
    }
}
