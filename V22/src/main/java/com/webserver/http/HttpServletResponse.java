package com.webserver.http;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    private ByteArrayOutputStream baos;
    private byte[] contentData;//作为响应正文数据的字节数组

    public HttpServletResponse(Socket socket){
        this.socket = socket;
    }

    /**
     * 将当前响应对象内容按照标准的HTTP响应格式发送给客户端
     */
    public void response() throws IOException {
        //发送前的准备工作
        sendBefore();
        //3.1发送状态行
        sendStatusLine();
        //3.2发送响应头
        sendHeaders();
        //3.3发送响应正文
        sendContent();
    }

    /**
     * 发送前最后的准备工作
     */
    private void sendBefore(){
        //先判断baos是否为null.如果不是null则说明存在动态数据
        if(baos!=null){
            //得到其内部的字节数组(动态数据)
            contentData = baos.toByteArray();
        }
        //如果动态数据数组不为null,添加响应头Content-Length
        if(contentData!=null){
            addHeader("Content-Length",contentData.length+"");
        }
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
        if(contentData!=null){//有动态数据
            OutputStream out = socket.getOutputStream();
            out.write(contentData);
        }else if(contentFile!=null) {
            OutputStream out = socket.getOutputStream();
            try(
                FileInputStream fis = new FileInputStream(contentFile);
            ) {
                int len;
                byte[] data = new byte[1024 * 10];
                while ((len = fis.read(data)) != -1) {
                    out.write(data, 0, len);
                }
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
        String contentType = null;
        try {
            contentType = Files.probeContentType(contentFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
            当我们无法根据正文文件确定类型时，则不发送Content-Type
            在HTTP协议中当响应包含正文，但是不包含Content-Type时，则表示让浏览器
            自行判断响应正文内容
         */
        if(contentType!=null) {
            addHeader("Content-Type", contentType);
        }
        addHeader("Content-Length",contentFile.length()+"");
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

    /**
     * 返回一个字节输出流，通过这个流写出的数据最终会作为响应正文内容发送给浏览器
     * 只要响应对象相同，无论调用多少次该方法，返回的始终是同一个输出流。
     * @return
     */
    public OutputStream getOutputStream(){
        if(baos==null){
            baos = new ByteArrayOutputStream();
        }
        return baos;
    }

    /**
     * 通过该方法获取的缓冲字符输出流写出的文本会作为正文内容发送给浏览器
     * @return
     */
    public PrintWriter getWriter(){
        return new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                getOutputStream(),
                                StandardCharsets.UTF_8
                        )
                ),true
        );
    }

    /**
     * 设置响应头Content-Type的值
     * @param mime
     */
    public void setContentType(String mime){
        addHeader("Content-Type",mime);
    }

}




