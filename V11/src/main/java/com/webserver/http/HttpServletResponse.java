package com.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 响应对象
 * 该类的每一个实例用于表示HTTP协议要求的响应
 * 每个响应由三部分组成:
 * 状态行，响应头，响应正文
 */
public class HttpServletResponse {
    private Socket socket;
    //状态行相关信息
    private int statusCode = 200;
    private String statusReason = "OK";

    //响应头相关信息
    private Map<String,String> headers = new HashMap<>();

    //响应正文相关信息
    private File contentFile;


    public HttpServletResponse(Socket socket){
        this.socket = socket;
    }

    /**
     * 将当前响应对象内容按照标准的HTTP响应格式发送给客户端
     */
    public void response() throws IOException {
        //3.1发送状态行
        sendStatusLine();
        //3.2发送响应头
        sendHeaders();
        //3.3发送响应正文
        sendContent();
    }

    /**
     * 发送状态行
     */
    private void sendStatusLine() throws IOException {
        println("HTTP/1.1"+" "+statusCode+" "+statusReason);
    }
    /**
     * 发送响应头
     */
    private void sendHeaders() throws IOException {
        /*
            headers这个Map中的内容:
            key              value
            Content-Type     text/html
            Content-Length   224
            Server           WebServer
            ...              ...
         */
        //遍历headers将所有的响应头发送给客户端
        Set<Map.Entry<String,String>> entrySet = headers.entrySet();
        for(Map.Entry<String,String> e : entrySet){
            String key = e.getKey();
            String value = e.getValue();
            println(key + ": " + value);
        }

        println("");//单独发送回车换行表示响应头发送完毕了
    }

    /**
     * 发送响应正文
     */
    private void sendContent() throws IOException {
        OutputStream out = socket.getOutputStream();
        FileInputStream fis = new FileInputStream(contentFile);
        int len;
        byte[] data = new byte[1024*10];
        while((len = fis.read(data))!=-1){
            out.write(data,0,len);
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

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public File getContentFile() {
        return contentFile;
    }

    public void setContentFile(File contentFile) {
        this.contentFile = contentFile;
    }

    /**
     * 添加一个要发送的响应头
     * @param name
     * @param value
     */
    public void addHeader(String name,String value){
        //将响应头的名字和值以key-value形式存入headers这个Map中
        headers.put(name,value);
    }
}




