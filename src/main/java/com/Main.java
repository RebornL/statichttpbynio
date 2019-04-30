package com;

import com.statichttp.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

public class Main {

    static Charset charset = Charset.forName("utf-8");

    public static void main(String[] args) {
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8888));
            ssc.configureBlocking(false);

            Selector selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer readBuff = ByteBuffer.allocate(1024);
            ByteBuffer writeBuff = ByteBuffer.allocate(1024);
            System.out.println(LocalDateTime.now()+": Http Server start at 127.0.0.1:8888");
            while (true) {

                int nReady = selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isAcceptable()) {
                        System.out.println(LocalDateTime.now() + ": server accept");
                        SocketChannel socketChannel = ssc.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println(LocalDateTime.now() + ": "+ socketChannel.getRemoteAddress() + " link in");
                        SelectionKey connectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
                        connectionKey.attach(new HttpHandler(socketChannel, connectionKey));
                        connectionKey.cancel();
                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        System.out.println(LocalDateTime.now() + ": read from "+ socketChannel.getRemoteAddress());

                        readBuff.clear();
                        socketChannel.read(readBuff);
                        readBuff.flip();
                        String[] request = charset.decode(readBuff).toString().split("\n");
                        System.out.println(LocalDateTime.now() + ": " + request[0]);

                        key.interestOps(SelectionKey.OP_WRITE);
                    } else if (key.isWritable()) {
//                        System.out.println(System.getProperty("user.dir"));
                        FileChannel fileChannel = FileChannel.open(Paths.get("E:\\Files\\project\\Test2\\src\\nio\\128N.jpg"));

//                        writeBuff.rewind();
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        System.out.println(LocalDateTime.now() + ": response to "+socketChannel.getRemoteAddress());
                        socketChannel.write(ByteBuffer.wrap("Http/1.1 200 OK\n".getBytes()));
                        socketChannel.write(ByteBuffer.wrap("content-type: image/jpeg; \n".getBytes()));
                        socketChannel.write(ByteBuffer.wrap(("content-length: "+fileChannel.size()+"\n\n").getBytes()));

                        while (fileChannel.read(writeBuff) != -1) {
                            writeBuff.flip();
                            socketChannel.write(writeBuff);
                            writeBuff.clear();
                        }
                        System.out.println(LocalDateTime.now()+": "+socketChannel.getRemoteAddress()+" close");
                        socketChannel.close();

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
