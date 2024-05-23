package com.yupi.yurpc.server;

import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.serializer.Serializer;
import com.yupi.yurpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * HTTP 请求处理
 * 业务流程：
 *  1. 反序列化请求变为对象，从请求对象中获取参数
 *  2. 根据服务名称从本地注册器中获取到对应的服务实现类
 *  3. 通过反射机制调用方法，得到返回结果
 *  4. 对返回结果进行封装和序列化，写入响应中
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {


    @Override
    public void handle(HttpServerRequest request) {

        //指定序列化器
        //final Serializer serializer = new JdkSerializer();
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        //记录日志
        System.out.println("Received request:" + request.method() + "" + request.uri());

        // 异步处理HTTP请求
        request.bodyHandler((body)->{

            RpcRequest rpcRequest = null;
            //反序列化
            try {
                rpcRequest = serializer.deserialize(body.getBytes(), RpcRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();

            //请求可能为空
            if (rpcRequest == null){
                rpcResponse.setMessage("rpcRequest is null");
                doResponse(request,rpcResponse,serializer);
                return;
            }

            try {
                //通过反射调用服务实现类
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());

                //封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");

            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            //响应出去
            doResponse(request,rpcResponse,serializer);
        });

        //
    }

    /**
     * 响应数据
     * @param request 一个请求
     * @param rpcResponse 响应
     * @param serializer 序列化
     */
    private void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {

        HttpServerResponse httpServerResponse = request.response().putHeader("content-type", "application/json");

        //序列化出去
        byte[] serialized = new byte[0];
        try {
            serialized = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }

    }
}
