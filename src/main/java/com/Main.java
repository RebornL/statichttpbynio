package com;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {

    public static void main(String[] args) throws IOException {

//        FileChannel inChannel = null;
//        FileChannel outChannel = null;
//        Path pathIn = Paths.get(System.getProperty("user.dir"), "static/test.jpg");
//        Path pathOut = Paths.get(System.getProperty("user.dir"), "static/testOut.jpg");
//        File file = pathOut.toFile();
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                System.out.println("创建文件失败");
//                e.printStackTrace();
//            }
//        }
//        try {
//            inChannel = FileChannel.open(pathIn);
//            outChannel = new RandomAccessFile(file, "rw").getChannel();
//
//            ByteBuffer buf = ByteBuffer.allocate(1024);
//            while (inChannel.read(buf) != -1) {
//                buf.flip();
//                outChannel.write(buf);
//                buf.clear();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // 1.获取通道
        ServerSocketChannel server = ServerSocketChannel.open();

        // 2.得到文件通道
        FileChannel inChannel = FileChannel.open(Paths.get(System.getProperty("user.dir"), "static/test.jpg"));
//        inChannel.size();
        // 3. 绑定链接
        server.bind(new InetSocketAddress(55555));

        // 4. 获取客户端的连接(阻塞的)
        SocketChannel client = server.accept();

        // 5. 要使用NIO，有了Channel，就必然要有Buffer，Buffer是与数据打交道的呢
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        client.write(ByteBuffer.wrap("HTTP/1.1 200 OK".getBytes()));
        client.write(ByteBuffer.wrap("Content-Type: image/jpeg;charset= UTF-8".getBytes()));
        client.write(ByteBuffer.wrap(("Content-Length: "+inChannel.size()+"\n").getBytes()));
        // 6.将客户端传递过来的图片保存在本地中
        while (inChannel.read(buffer) != -1) {

            // 在读之前都要切换成读模式
            buffer.flip();

            client.write(buffer);

            // 读完切换成写模式，能让管道继续读取文件的数据
            buffer.clear();

        }

        // 7.关闭通道
        inChannel.close();
        client.close();
        server.close();
    }
}
