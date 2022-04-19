package com.webserver.core;

import com.webserver.http.HttpServletRequest;

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
            //1 解析请求
            HttpServletRequest request = new HttpServletRequest(socket);

            //2 处理请求
            String path = request.getUri();
            File root = new File(ClientHandler.class.getClassLoader().getResource(".").toURI());
            File staticDir = new File(root,"static");
            File file = new File(staticDir,path);
            System.out.println("资源是否存在:"+file.exists());

            int statusCode;//状态代码
            String statusReason;//状态描述
            if(file.isFile()){//当file表示的文件真实存在且是一个文件时返回true
                statusCode = 200;
                statusReason = "OK";
            }else{//要么file表示的是一个目录，要么不存在
                statusCode = 404;
                statusReason = "NotFound";
                file = new File(staticDir,"root/404.html");
            }

            //3 发送响应
            //3.1发送状态行
            println("HTTP/1.1"+" "+statusCode+" "+statusReason);
            //3.2发送响应头
            println("Content-Type: text/html");
            println("Content-Length: "+file.length());
            println("");
            //3.3发送响应正文(index.html页面的数据)
            OutputStream out = socket.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            int len;
            byte[] data = new byte[1024*10];
            while((len = fis.read(data))!=-1){
                out.write(data,0,len);
            }

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

    /**
     * 将一行字符串按照ISO8859-1编码发送给客户端,最后会自动添加回车和换行符
     * @param line
     */
    private void println(String line) throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] data = line.getBytes(StandardCharsets.ISO_8859_1);
        out.write(data);
        out.write(13);//单独发送了回车符
        out.write(10);//单独发送换行符
    }


}
