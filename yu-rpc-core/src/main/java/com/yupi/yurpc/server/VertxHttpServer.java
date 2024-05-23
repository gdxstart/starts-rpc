package com.yupi.yurpc.server;

import io.vertx.core.Vertx;

/**
 * 基于Vertx实现web服务器：监听指定端口 并处理请求
 */
public class VertxHttpServer implements HttpServer{
    /**
     * 启动服务器
     * @param port 端口号
     */
    @Override
    public void doStart(int port) {

        // 1.创建一个Vertx(已经导入依赖)
        Vertx vertx = Vertx.vertx();
        // 2.创建一个Http服务器
        io.vertx.core.http.HttpServer httpServer = vertx.createHttpServer();
        // 3.监听端口号并处理请求
        /**
        httpServer.requestHandler(httpServerRequest -> {
            // 处理http请求
            System.out.println("Received request :" + httpServerRequest.method() + httpServerRequest.uri());

            // 发送http响应
            httpServerRequest.response().putHeader("content-type","text/plain").end("Hello from Vert.x HTTP server!");
        });**/
        httpServer.requestHandler(new HttpServerHandler());

        // 4.启动Http服务器并监听指定端口
        httpServer.listen(port,result ->{
            if (result.succeeded()){
                System.out.println("Server is now listening on  port " + port);
            }else {
                System.out.println("Failed to start server : " + result.cause());
            }
        });
    }
}
