package com.yupi.example.consumer;

import com.yupi.example.common.model.User;
import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.bootstrap.ConsumerBootstrap;
import com.yupi.yurpc.proxy.ServiceProxyFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 简单服务消费者示例
 */
@Slf4j
public class ConsumerExample {

    public static void main(String[] args) {
//        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
//        System.out.println(rpc);
        //服务消费者初始化
        ConsumerBootstrap.init();
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
//        log.info("ssssssss",String.valueOf(userService.getNumber()));
        //获取一个用户
        User user = new User();
        user.setName("yupi");
        //调用服务
        User newUser = userService.getUser(user);
        if (newUser != null){
            //服务提供者有该用户
            System.out.println("用户名：" + newUser.getName());
        }else {
            System.out.println("user == null");
        }
        long number = userService.getNumber();
        System.out.println(number);
    }
}
