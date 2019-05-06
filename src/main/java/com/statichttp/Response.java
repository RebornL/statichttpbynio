package com.statichttp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Response {

    private String encoding = "UTF-8";
    private SocketChannel socketChannel;

    public Response(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void sendData(Path filePath, String contentType, String md5) throws IOException {
        FileChannel fileChannel = FileChannel.open(filePath);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        socketChannel.write(ByteBuffer.wrap("HTTP/1.1 200 OK\r\n".getBytes()));
        socketChannel.write(ByteBuffer.wrap(("Content-Type: " + contentType + ";charset=" + encoding+"\r\n").getBytes()));
        socketChannel.write(ByteBuffer.wrap(("Content-Length: " + fileChannel.size()+"\r\n").getBytes()));
        socketChannel.write(ByteBuffer.wrap("Cache-Control: max-age=1234567, private, must-revalidate\r\n".getBytes()));
        socketChannel.write(ByteBuffer.wrap(("ETag: "+md5+"\r\n").getBytes()));
        socketChannel.write(ByteBuffer.wrap(("\r\n").getBytes()));

        while (fileChannel.read(byteBuffer) > 0) {
            byteBuffer.flip();
            if(socketChannel.write(byteBuffer) <= 0) {
                System.out.println(byteBuffer.limit());
                System.out.println(LocalDateTime.now()+": 客户端关闭连接，无法写入数据");
//                socketChannel.close();
                break;
            }
            byteBuffer.clear();
        }



    }

    public void response304() throws IOException {
        socketChannel.write(ByteBuffer.wrap("HTTP/1.1 304 Not Modified\r\n".getBytes()));
    }

    public void Response404() throws IOException {
        socketChannel.write(ByteBuffer.wrap("HTTP/1.1 404 NOTFOUND\r\n".getBytes()));
        socketChannel.write(ByteBuffer.wrap("Content-Type:text/html;charset=UTF-8\r\n".getBytes()));
        socketChannel.write(ByteBuffer.wrap(("\r\n").getBytes()));
        socketChannel.write(ByteBuffer.wrap(("该资源在服务器中不存在").getBytes()));
    }
}
