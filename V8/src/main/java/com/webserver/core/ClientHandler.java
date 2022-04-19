package com.webserver.core;

import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * 该线程任务负责与指定客户端完成HTTP交互
 * 与客户端交流的流程分成三步:
 * 1:解析请求
 * 2:处理请求
 * 3:发送响应
 */
public class ClientHandler implements Runnable{
    private Socket socket;
    public  ClientHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            /*
                http://localhost:8088/myweb/index.html

                http://localhost:8088/myweb/123.html

             */
            //1 解析请求
            HttpServletRequest request = new HttpServletRequest(socket);
            HttpServletResponse response = new HttpServletResponse(socket);

            //2 处理请求
            String path = request.getUri();
            File root = new File(ClientHandler.class.getClassLoader().getResource(".").toURI());
            File staticDir = new File(root,"static");
            File file = new File(staticDir,path);
            System.out.println("资源是否存在:"+file.exists());

            if(file.isFile()){//当file表示的文件真实存在且是一个文件时返回true
                response.setContentFile(file);

            }else{//要么file表示的是一个目录，要么不存在
                response.setStatusCode(404);
                response.setStatusReason("NotFound");
                file = new File(staticDir,"root/404.html");
                response.setContentFile(file);
            }

            //3 发送响应
            response.response();

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        } finally {
            //HTTP协议要求，响应完客户端后就要断开TCP连接
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}
