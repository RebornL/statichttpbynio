package com.statichttp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Property {
    public static ConcurrentHashMap<String, String> FILE2MD5 = new ConcurrentHashMap<>(100);

    public static String STATIC_ROOT = System.getProperty("user.dir") + File.separator + "static";
    public static String HOSTNAME = "";
    public static int PORT = 22222;

    public static final Charset CHARSET = Charset.forName("utf-8");

    static {
        Properties props = new Properties();
        InputStream fis = HttpServer.class.getResourceAsStream("../../config.properties");
        try {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("配置文件读取出错");
            e.printStackTrace();
        }
        HOSTNAME = props.getProperty("hostname");
        PORT = Integer.parseInt(props.getProperty("port"));
        STATIC_ROOT = System.getProperty("user.dir")+ File.separator+props.getProperty("static_path");
    }

    private Property() {}
}
