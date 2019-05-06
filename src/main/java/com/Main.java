package com;

import com.statichttp.HttpServer;
import com.statichttp.Property;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            HttpServer server = new HttpServer(Property.HOSTNAME, Property.PORT);
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
