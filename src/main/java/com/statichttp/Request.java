package com.statichttp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Request {

    private SocketChannel socketChannel;
    // 请求资源
    private String uri;
    // 资源后缀
    private String suffix;
    // 请求头部信息
    private HashMap<String, String> req = new HashMap<>();

    public Request(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
//        try {
//            parse();
//        } catch (IOException e) {
//            System.out.println(LocalDateTime.now()+": 解析头部参数出错");
//            e.printStackTrace();
//        }
    }

    public void parse() throws IOException {
        ByteBuffer readBuff = ByteBuffer.allocate(1024);
        readBuff.clear();
        String request = "";
        int readByte = 0;
        while ((readByte = socketChannel.read(readBuff)) > 0) {
            readBuff.flip();
            request += Property.CHARSET.decode(readBuff).toString();
            readBuff.clear();
        }
        String[] requestHeader = request.split("\r\n");
        this.uri = ParseUri(requestHeader[0]);
        this.suffix = GetSuffix(uri);
        // 读取所有浏览器发送过来的请求参数头部的所有信息
        for (int i = 1; i < requestHeader.length; i++) {
            String[] headerKV = requestHeader[i].split(":");
            if (headerKV.length > 0) {
                req.put(headerKV[0], headerKV[1].trim());//strip是jdk11的方法
            }
        }

    }

    private static String ParseUri(String request) {
        if (request == null) {
            return null;
        }
        int index1, index2;
        index1 = request.indexOf(' ');
        if (index1 != -1) {
            index2 = request.indexOf(' ', index1+1);
            if (index2>index1) {
                return request.substring(index1+1, index2);
            }
        }
        return null;
    }

    private static String GetSuffix(String resource) {
        String suffix = null;
        if (resource.equals("/")) {
            resource = "/index.html";
            String[] names = resource.split("\\.");
            suffix = names[names.length - 1];
        } else {
            String[] names = resource.split("\\.");
            suffix = names[names.length - 1];
        }
        return suffix;
    }


    public String getUri() {
        return uri;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getHeader(String param) {
        return req.getOrDefault(param, null);
    }
}
