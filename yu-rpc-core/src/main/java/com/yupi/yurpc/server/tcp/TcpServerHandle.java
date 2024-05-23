package com.yupi.yurpc.server.tcp;

import com.yupi.yurpc.protocol.ProtocolMessage;
import com.yupi.yurpc.protocol.ProtocolMessageEncoder;
import com.yupi.yurpc.protocol.ProtocolMessageTypeEnum;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.protocol.ProtocolMessageDecoder;
import com.yupi.yurpc.registry.LocalRegistry;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.Handler;

import java.lang.reflect.Method;

/**
 * TCP 请求处理器
 */
public class TcpServerHandle implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket netSocket) {

        // 处理请求
        /**
         netSocket.handler(buffer -> {
         ProtocolMessage<RpcRequest> message;
         // 1.接收请求,进行解码
         try{
         message = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
         }catch (Exception e){
         throw new RuntimeException("协议消息解码错误");
         }
         RpcRequest rpcRequest = message.getBody();

         // 2.处理请求,构造响应结果对象
         RpcResponse rpcResponse = new RpcResponse();
         try{
         //获取要调用的服务实现类,通过反射调用
         Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
         Method method = implClass.getMethod(rpcRequest.getMethodName());
         Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());

         //封装响应结果
         rpcResponse.setData(result);
         rpcResponse.setDataType(method.getReturnType());
         rpcResponse.setMessage("ok");
         }catch (Exception e){
         e.printStackTrace();
         rpcResponse.setMessage(e.getMessage());
         rpcResponse.setException(e);
         }

         // 3.发送响应,编码
         ProtocolMessage.Header header = message.getHeader();
         header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());

         ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
         try{
         Buffer encode = ProtocolMessageEncoder.encode(rpcResponseProtocolMessage);
         netSocket.write(encode);
         }catch (Exception e){
         throw new RuntimeException("协议消息编码错误");
         }

         });**/
        TcpBufferHandleWrapper bufferHandleWrapper = new TcpBufferHandleWrapper(buffer -> {

            ProtocolMessage<RpcRequest> message;
            // 1.接收请求,进行解码
            try {
                message = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (Exception e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = message.getBody();

            // 2.处理请求,构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            try {
                //获取要调用的服务实现类,通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());

                //封装响应结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 3.发送响应,编码
            ProtocolMessage.Header header = message.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());

            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(rpcResponseProtocolMessage);
                netSocket.write(encode);
            } catch (Exception e) {
                throw new RuntimeException("协议消息编码错误");
            }

        });
        netSocket.handler(bufferHandleWrapper);
    }
}
