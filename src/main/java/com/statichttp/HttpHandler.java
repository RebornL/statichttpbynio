package com.statichttp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ResponseCache;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        this.request = new Request(socketChannel);
        this.response = new Response(socketChannel);
    }

    public void handlerRequest() {
        try {
            request.parse();
        } catch (IOException e) {
            System.out.println(LocalDateTime.now()+": 解析Request的信息出错");
            e.printStackTrace();
        }
        System.out.println(LocalDateTime.now() + ": " + getUri());
        connectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    public void handlerResponse() throws IOException {
        Path filePath = Paths.get(Property.STATIC_ROOT, getUri());
        if (Files.exists(filePath)) {
            String md5 = Property.FILE2MD5.getOrDefault(getUri(), null);
            if (md5 == null) {
                try {
                    md5 = MD5Util.md5HashCode32(filePath.toString());
                    Property.FILE2MD5.put(getUri(), md5);
                } catch (FileNotFoundException e) {
                    System.out.println(LocalDateTime.now()+": 生成md5失败");
                    e.printStackTrace();
                }
            }
            response.sendData(filePath, request.getSuffix(), md5);
        } else {
            response.Response404();
        }


    }

    public void close() {
        SocketAddress socketAddress = null;
        try {
            socketAddress = socketChannel.getRemoteAddress();
            socketChannel.close();
//            connectionKey.cancel();
            socketAddress = null;
        } catch (IOException e) {
            System.out.println(LocalDateTime.now()+": "+socketAddress+" close error");
            e.printStackTrace();
        }
    }

    public String getUri() {
        return request.getUri();
    }

    private String getSuffix() {
        return request.getSuffix();
    }
}
