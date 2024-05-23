package com.yupi.yurpc.moder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 封装调用方法时得到的返回信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {
    //响应数据
    private Object data;
    //响应数据流类型（预留）
    private Class<?> dataType;
    //响应信息
    private String message;
    //异常处理
    private Exception exception;
}
