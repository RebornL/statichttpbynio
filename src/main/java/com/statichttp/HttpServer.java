package com.statichttp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
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

        System.out.println(LocalDateTime.now()+": Http Server start at "+this.hostname+":"+this.port);
        while (isRunning) {

            int nReady = selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();

            while (isRunning && it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isAcceptable()) {
                    System.out.println(LocalDateTime.now() + ": server accept");
                    SocketChannel socketChannel = ssc.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println(LocalDateTime.now() + ": "+ socketChannel.getRemoteAddress().toString().substring(1) + " link in");
                    SelectionKey connectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
                    connectionKey.attach(new HttpHandler(socketChannel, connectionKey));
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    System.out.println(LocalDateTime.now() + ": read from "+ socketChannel.getRemoteAddress());
                    HttpHandler handler = (HttpHandler)key.attachment();
                    handler.handlerRequest();
                    if (handler.getUri().equals(SHUTDOWN_COMMAND)) {
                        handler.closeServerByWeb();
                        this.isRunning = false;
                        break;
                    }

                } else if (key.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    System.out.println(LocalDateTime.now()+": send static file to "+socketChannel.getRemoteAddress());
                    HttpHandler handler = (HttpHandler)key.attachment();
                    handler.handlerResponse();
                    handler.close();

                }
            }
        }
        ssc.close();
        selector.close();
        System.out.println(LocalDateTime.now()+": http server stop");
    }

}
