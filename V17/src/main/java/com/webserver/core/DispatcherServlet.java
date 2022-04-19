package com.webserver.core;

import com.webserver.controller.UserController;
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
        String path = request.getRequestURI();

        System.out.println("====================>"+path);

        //首先判断该请求是否为请求一个业务
        if("/myweb/reg".equals(path)){
            System.out.println("!!!!!!!!!!!!!!!开始处理注册");
            UserController controller = new UserController();
            controller.reg(request, response);


        }else if("/myweb/login".equals(path)){
            System.out.println("!!!!!!!!!!!!!!!开始处理登录");
            UserController controller = new UserController();
            controller.login(request, response);

        }else {
            File file = new File(staticDir, path);
            System.out.println("资源是否存在:" + file.exists());
            if (file.isFile()) {//当file表示的文件真实存在且是一个文件时返回true
                response.setContentFile(file);

            } else {//要么file表示的是一个目录，要么不存在
                response.setStatusCode(404);
                response.setStatusReason("NotFound");
                file = new File(staticDir, "root/404.html");
                response.setContentFile(file);
            }
        }

        //测试添加一个额外的响应头
        response.addHeader("Server","WebServer");

    }
}






