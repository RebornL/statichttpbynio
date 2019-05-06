package com.statichttp;

import java.io.FileNotFoundException;
import java.io.IOException;
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
        String md5 = Property.FILE2MD5.getOrDefault(getUri(), null);

        if (null == request.getHeader("If-Modified-Since")) {
            // 第一次请求
            if (Files.exists(filePath)) {
                if (md5 == null) {
                    try {
                        md5 = MD5Util.md5HashCode32(filePath.toString());
                        Property.FILE2MD5.put(getUri(), md5);
                        System.out.println(LocalDateTime.now()+": 将"+getUri().substring(1)+"的md5("+md5+")添加到FILE2MD5中");
                    } catch (FileNotFoundException e) {
                        System.out.println(LocalDateTime.now()+": 生成md5失败");
                        e.printStackTrace();
                    }
                }
                response.sendData(filePath, MIME.getContentType(getSuffix()), md5, filePath.toFile().lastModified());
            } else {
                response.Response404();
            }
        } else {
            // 同一个再次请求
            if (filePath.toFile().lastModified() != Long.parseLong(request.getHeader("If-Modified-Since"))) {
                // 当文件被修改时
                String md5Temp = MD5Util.md5HashCode32(filePath.toString());
                if (md5Temp.equals(md5)) {
                    System.out.println(LocalDateTime.now()+": "+getUri().substring(1)+"的md5("+md5+")没有变化，只更新modified的时间");
                    response.sendData(filePath, MIME.getContentType(getSuffix()), md5, filePath.toFile().lastModified());
                } else {
                    Property.FILE2MD5.put(getUri(), md5Temp); // 更新md5的值
                    System.out.println(LocalDateTime.now()+": "+getUri().substring(1)+"的md5("+md5Temp+")发生变化，更新modified的时间和ETag的值");
                    response.sendData(filePath, MIME.getContentType(getSuffix()), md5Temp, filePath.toFile().lastModified());

                }
            } else {
                response.response304();
            }
        }



    }

    public void close() {
        SocketAddress socketAddress = null;
        try {
            socketAddress = socketChannel.getRemoteAddress();
            socketChannel.close();
//            connectionKey.cancel();
            System.out.println(LocalDateTime.now()+": "+socketAddress+" close");
            socketAddress = null;
        } catch (IOException e) {
            System.out.println(LocalDateTime.now()+": "+socketAddress+" close error");
            e.printStackTrace();
        }
    }

    public void closeServerByWeb() throws IOException {
        response.sendData("成功关闭静态资源服务器");
        close();
    }

    public String getUri() {
        return request.getUri();
    }

    private String getSuffix() {
        return request.getSuffix();
    }
}
