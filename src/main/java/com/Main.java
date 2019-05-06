package com;

import com.statichttp.HttpServer;
import com.statichttp.Property;

import java.io.IOException;
import java.nio.charset.Charset;

public class Main {

    static Charset charset = Charset.forName("utf-8");

    public static void main(String[] args) {
        try {
            HttpServer server = new HttpServer(Property.HOSTNAME, Property.PORT);
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
