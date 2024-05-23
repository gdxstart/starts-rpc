package com.yupi.yurpc.moder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 封装调用服务时所需的信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    // 服务名称
    private String serviceName;

    //方法名称
    private String methodName;

    //参数类型列表
    private Class<?>[] parameterTypes;

    //参数列表
    private Object[] args;
}
