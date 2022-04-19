package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求对象
 * 该类的每一个实例用于表示一个HTTP请求内容
 * 每个请求由三部分构成:
 * 请求行，消息头，消息正文
 */
public class HttpServletRequest {
    private Socket socket;
    //请求行相关信息
    private String method;//请求方式
    private String uri;//抽象路径
    private String protocol;//协议版本

    //消息头相关信息
    private Map<String,String> headers = new HashMap<>();

    public HttpServletRequest(Socket socket) throws IOException, EmptyRequestException {
        this.socket = socket;
        //1.1解析请求行
        parseRequestLine();
        //1.2解析消息头
        parseHeaders();
        //1.3解析消息正文
        parseContent();
    }

    /**
     * 解析请求行
     * parse:解析
     */
    private void parseRequestLine() throws IOException, EmptyRequestException {
        String line = readLine();

        if(line.isEmpty()){//如果请求行为空字符串，则说明为空请求
            throw new EmptyRequestException();
        }

        System.out.println(line);

        String[] array = line.split("\\s");
        method = array[0];
        uri = array[1];//这里可能会出现数组下标越界异常!原因:浏览器空请求，后期会解决
        protocol = array[2];
        System.out.println("method:"+method);    //GET
        System.out.println("uri:"+uri);          // /myweb/index.html
        System.out.println("protocol:"+protocol);//HTTP/1.1
    }
    /**
     * 解析消息头
     */
    private void parseHeaders() throws IOException {
        while(true) {
            String line = readLine();
            if(line.isEmpty()){
                break;
            }
            System.out.println("消息头:" + line);
            //将消息头按照"冒号空格"拆分为消息头的名字和值并以key,value存入headers
            String[] array = line.split(":\\s");
            headers.put(array[0],array[1]);
        }
        System.out.println("headers:"+headers);
    }
    /**
     * 解析消息正文
     */
    private void parseContent(){}


    /**
     * 通过socket获取的输入流读取客户端发送过来的一行字符串
     * @return
     */
    private String readLine() throws IOException {
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
        return builder.toString().trim();
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }
    /*
        根据消息头的名字获取对应消息头的值
     */
    public String getHeader(String name) {
        return headers.get(name);
    }
}
