package com.statichttp;

import java.io.IOException;
import java.net.ResponseCache;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;

public class HttpHandler {
    private SocketChannel socketChannel;
    private SelectionKey connectionKey;
    private Request request;
    private Response response;

    public HttpHandler(SocketChannel socketChannel, SelectionKey connectionKey) {
        this.socketChannel = socketChannel;
        this.connectionKey = connectionKey;
        init();
    }

    private void init() {
        // 初始化操作
        // 获取对应的request和response
        // 主要是request，获取客户端请求
    }

    public void close() {
        SocketAddress socketAddress = null;
        try {
            socketAddress = socketChannel.getRemoteAddress();
            socketChannel.close();
            connectionKey.cancel();
        } catch (IOException e) {
            System.out.println(LocalDateTime.now()+": "+socketAddress+" close error");
            e.printStackTrace();
        }
    }
}
