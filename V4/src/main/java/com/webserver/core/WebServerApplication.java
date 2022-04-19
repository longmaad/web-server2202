package com.webserver.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * WebServer主类
 * WebServer是一个Web容器，实现了Tomcat中的基础功能，通过这个项目的学习了解Tomcat的
 * 底层工作逻辑。
 * Web容器是一个Web服务端程序，主要负责两方面的工作:
 * 1:管理部署在容器中的所有网络应用
 * 2:与客户端(通常就是浏览器)进行TCP通讯，并基于HTTP协议进行应用交互，使得客户端可以
 *   通过网络远程调用容器下不同网络应用中的内容。
 *
 * 网络应用(webapp):包含的内容大致有:网页，处理业务的代码，其他资源等。就是俗称的"网站"
 */
public class WebServerApplication {
    private ServerSocket serverSocket;

    public WebServerApplication(){
        try {
            System.out.println("正在启动服务端...");
            serverSocket = new ServerSocket(8088);
            System.out.println("服务端启动完毕!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        try {
            System.out.println("等待客户端连接...");
            Socket socket = serverSocket.accept();
            System.out.println("一个客户端连接了!");
            //启动一个线程处理该客户端交互
            ClientHandler handler = new ClientHandler(socket);
            Thread t = new Thread(handler);
            t.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WebServerApplication application = new WebServerApplication();
        application.start();
    }
}
