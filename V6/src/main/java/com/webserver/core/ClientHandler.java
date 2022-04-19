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
            // http://localhost:8088/myweb/index.html
            // http://localhost:8088/myweb/classTable.html
            //通过请求对象，获取浏览器地址栏中的抽象路径部分
            String path = request.getUri();//path:/myweb/index.html



            //3 发送响应
            //定位要发送的文件(将src/main/resources/static/myweb/index.html)
            /*
                定义为resource目录(maven项目中src/main/java和src/main/resources实际上是一个目录)
                只不过java目录中存放的都是.java的源代码文件
                而resources目录下存放的就是非.java的其他程序中需要用到的资源文件

                实际开发中，我们在定位目录时，常使用相对路径，而实际应用的相对路径都是类加载路径
                类加载路径可以用:
                类名.class.getClassLoader().getResource(".")就是类加载路径
                这里可以理解为时src/main/java目录或src/main/resources
                实际表达的是编译后这两个目录最终合并的target/classes目录。
             */
            File root = new File(
                    ClientHandler.class.getClassLoader().getResource(".").toURI()
            );
            /*
                root表达的是target/classes
                从根开始寻找static目录
             */
            File staticDir = new File(root,"static");
            /*
                在static目录下定位index.html页面
             */
            File file = new File(staticDir,path);

            System.out.println("资源是否存在:"+file.exists());

            /*
                响应大致内容:
                HTTP/1.1 200 OK(CRLF)
                Content-Type: text/html(CRLF)
                Content-Length: 2546(CRLF)(CRLF)
                1011101010101010101......
             */
            //3.1发送状态行
            println("HTTP/1.1 200 OK");
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
