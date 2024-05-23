package com.yupi.yurpc.proxy;

import cn.hutool.core.collection.CollUtil;
import com.yupi.yurpc.constant.RpcConstant;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.server.tcp.VertxTcpClient;
import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.config.RpcConfig;
import com.yupi.yurpc.fault.retry.RetryStrategy;
import com.yupi.yurpc.fault.retry.RetryStrategyFactory;
import com.yupi.yurpc.fault.tolerant.TolerantStrategy;
import com.yupi.yurpc.fault.tolerant.TolerantStrategyFactory;
import com.yupi.yurpc.loadbalancer.LoadBalancer;
import com.yupi.yurpc.loadbalancer.LoadBalancerFactory;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.ServiceMetaInfo;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.serializer.Serializer;
import com.yupi.yurpc.serializer.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * 服务代理（JDK动态代理）
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@code Method} instance corresponding to
     *               the interface method invoked on the proxy instance.  The declaring
     *               class of the {@code Method} object will be the interface that
     *               the method was declared in, which may be a superinterface of the
     *               proxy interface that the proxy class inherits the method through.
     * @param args   an array of objects containing the values of the
     *               arguments passed in the method invocation on the proxy instance,
     *               or {@code null} if interface method takes no arguments.
     *               Arguments of primitive types are wrapped in instances of the
     *               appropriate primitive wrapper class, such as
     *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //指定序列化器
//        Serializer serializer = new JdkSerializer();
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        //构造发送
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();


        byte[] bodyBytes = serializer.serialize(rpcRequest);
        // 地址硬编码 需要注册中心和服务发现 解决
//            try(HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
//                    .body(bodyBytes)
//                    .execute()){
//                byte[] result = httpResponse.bodyBytes();
//                // 反序列化
//                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
//                return  rpcResponse.getData();

        /* 从注册中心中获取服务提供者的请求地址 */
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        //TODO 暂时设置为本地
        serviceMetaInfo.setServiceHost("localhost");
        log.info(serviceMetaInfo + "iiiiiiiiiiiii");
        List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfos)) {
            throw new RuntimeException("暂无服务地址");
        }

        // 暂时取第一个
//            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfos.get(0);

        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        // 将调用方法名(请求路径)作为负载均衡参数
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfos);


        // 发送请求
        /**
         try(
         HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
         .body(bodyBytes)
         .execute()
         ){
         byte[] result = httpResponse.bodyBytes();

         // 反序列化
         RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
         return rpcResponse.getData();
         }


         }catch (IOException e){
         e.printStackTrace();
         }**/
        //发送TCP请求
        /**
         Vertx vertx = Vertx.vertx();
         NetClient netClient = vertx.createNetClient();
         CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();

         netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getServiceHost(), result -> {

         if (result.succeeded()) {
         System.out.println("Connected to TCP server");
         io.vertx.core.net.NetSocket socket = result.result();

         // 发送数据,构造消息
         ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
         ProtocolMessage.Header header = new ProtocolMessage.Header();
         header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
         header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
         //TODO 无法进行转为byte
         String serializerd = RpcApplication.getRpcConfig().getSerializer();
         header.setSerializer((byte) ProtocolMessageSerializeEnum.getEnumByValue(serializerd).getKey());
         header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
         header.setRequestId(IdUtil.getSnowflakeNextId());
         protocolMessage.setHeader(header);
         protocolMessage.setBody(rpcRequest);

         // 编码请求
         try {
         Buffer encode = ProtocolMessageEncoder.encode(protocolMessage);
         socket.write(encode);
         } catch (Exception e) {
         throw new RuntimeException("协议消息编码错误");
         }

         // 接收响应
         socket.handler(buffer -> {
         try {
         ProtocolMessage<RpcResponse> message = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
         responseFuture.complete(message.getBody());
         } catch (Exception e) {
         throw new RuntimeException("协议消息编码错误");
         }
         });
         } else {
         System.out.println("Failed to connect to TCP server");
         }
         });

         RpcResponse rpcResponse = responseFuture.get();

         netClient.close();
         return rpcResponse.getData();
         } catch (IOException e) {
         e.printStackTrace();
         }**/
        //发送TCP请求:封装
        RpcResponse rpcResponse;
        try {
            // 使用重试机制
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );
        } catch (Exception e) {
            // 容错机制
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }
        return rpcResponse.getData();
    }
}
