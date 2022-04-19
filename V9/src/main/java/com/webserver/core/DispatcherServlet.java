package com.webserver.core;

import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.net.URISyntaxException;

/**
 * 用于处理请求
 */
public class DispatcherServlet {
    private static File root;
    private static File staticDir;

    static {
        try {
            root = new File(DispatcherServlet.class.getClassLoader().getResource(".").toURI());
            staticDir = new File(root,"static");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void service(HttpServletRequest request, HttpServletResponse response){
        String path = request.getUri();

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
    }
}






