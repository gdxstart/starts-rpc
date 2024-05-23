package com.yupi.example.provider;

import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.server.VertxHttpServer;

/**
 * 简单的服务提供者示例
 */
public class EasyProviderExample {

    public static void main(String[] args) {
        //提供服务

        //注册服务（反射）
        LocalRegistry.register(UserService.class.getName(),UserServiceImpl.class);

        //启动 web 服务
        VertxHttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
