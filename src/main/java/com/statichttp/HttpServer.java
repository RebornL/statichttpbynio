package com.statichttp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

public class HttpServer {

    private static String SHUTDOWN_COMMAND = "/SHUTDOWN";

    private String hostname;
    private int port;

    private volatile boolean isRunning = true;

    public HttpServer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(this.hostname, this.port));
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer readBuff = ByteBuffer.allocate(1024);
        ByteBuffer writeBuff = ByteBuffer.allocate(1024);
        System.out.println(LocalDateTime.now()+": Http Server start at "+this.hostname+":"+this.port);
        while (isRunning) {

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
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    System.out.println(LocalDateTime.now() + ": read from "+ socketChannel.getRemoteAddress());
                    HttpHandler handler = (HttpHandler)key.attachment();
                    handler.handlerRequest();

                    System.out.println(LocalDateTime.now() + ": " + handler.getUri());
                    if (handler.getUri() == SHUTDOWN_COMMAND) {
                        handler.close();
                        this.isRunning = false;
                        break;
                    }

                } else if (key.isWritable()) {
                    System.out.println(Paths.get(Property.STATIC_ROOT, "test.jpg"));
                    FileChannel fileChannel = FileChannel.open(Paths.get(Property.STATIC_ROOT, "test.jpg")   );
//                        writeBuff.rewind();
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    System.out.println(LocalDateTime.now() + ": response to "+socketChannel.getRemoteAddress());
                    socketChannel.write(ByteBuffer.wrap("Http/1.1 200 OK\n".getBytes()));
                    socketChannel.write(ByteBuffer.wrap("content-type: image/jpeg; charset: utf-8\n".getBytes()));
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
        ssc.close();
        selector.close();
        System.out.println(LocalDateTime.now()+": http server stop");
    }

}