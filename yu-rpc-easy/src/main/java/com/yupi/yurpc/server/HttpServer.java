package com.yupi.yurpc.server;

/**
 * Http 服务器接口：统一管理服务器的启动、关闭，拓展实现多种不同的web服务器
 */
public interface HttpServer {
    /**
     * 启动服务器
     * @param port 端口号
     */
    void doStart(int port);
}
