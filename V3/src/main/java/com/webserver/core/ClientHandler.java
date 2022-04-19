package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

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
            //读取客户端发过来的消息
            InputStream in = socket.getInputStream();
            char pre='a',cur='a';//pre上次读取的字符，cur本次读取的字符
            StringBuilder builder = new StringBuilder();
            int d;
            while((d = in.read())!=-1){
                cur = (char)d;//本次读取到的字符
                if(pre==13&&cur==10){//判断是否连续读取到了回车和换行符
                    break;
                }
                builder.append(cur);
                pre = cur;//在进行下次读取字符前将本次读取的字符记作上次读取的字符
            }
            String line = builder.toString().trim();
            System.out.println(line);

            //请求行相关信息
            String method;//请求方式
            String uri;//抽象路径
            String protocol;//协议版本

            String[] array = line.split("\\s");
            method = array[0];
            uri = array[1];//这里可能会出现数组下标越界异常!原因:浏览器空请求，后期会解决
            protocol = array[2];
            // http://localhost:8088/myweb/index.html
            System.out.println("method:"+method);    //GET
            System.out.println("uri:"+uri);          // /myweb/index.html
            System.out.println("protocol:"+protocol);//HTTP/1.1




        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
