package com.webserver.controller;

import com.webserver.core.DispatcherServlet;
import com.webserver.entity.User;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理与用户相关的业务操作
 */
public class UserController {
    private static File userDir;
    private static File root;
    private static File staticDir;

    static {
        try {
            root = new File(DispatcherServlet.class.getClassLoader().getResource(".").toURI());
            staticDir = new File(root, "static");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        userDir = new File("./users");
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
    }

    public void reg(HttpServletRequest request, HttpServletResponse response) {
        //1 获取用户注册页面上输入的注册信息，获取form表单提交的内容
        /*
            getParameter传入的值必须和页面表单上对应输入框的名字一致
            即:<input name="username" type="text">
                            ^^^^^^^
                            以它一致
         */
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        String ageStr = request.getParameter("age");

        /*
            必要的验证，要求:
            四项信息不能为null，并且年龄必须是一个数字(正则表达式)
            否则直接给用户一个注册失败的页面:reg_input_error.html
            该页面剧中显示一行字:输入信息有误，注册失败。
            实现思路:
            添加一个分支判断，如果符合了上述的情况，直接创建一个File对象表示
            错误提示页面，然后将其设置到响应对象的正文上即可。否则才执行下面
            原有的注册操作。
         */
        if (username == null || password == null || nickname == null || ageStr == null ||
                !ageStr.matches("[0-9]+")) {
            File file = new File(staticDir, "/myweb/reg_input_error.html");
            response.setContentFile(file);
            return;
        }
        int age = Integer.parseInt(ageStr);
        System.out.println(username + "," + password + "," + nickname + "," + ageStr);


        //2 将用户信息保存
        File userFile = new File(userDir, username + ".obj");
        /*
            判断是否为重复用户，若重复用户，则直接响应页面:have_user.html
            该页面剧中显示一行字:该用户已存在，请重新注册
         */
        if (userFile.exists()) {//文件存在则说明是重复用户
            File file = new File(staticDir, "/myweb/have_user.html");
            response.setContentFile(file);
            return;
        }


        try (
                FileOutputStream fos = new FileOutputStream(userFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            User user = new User(username, password, nickname, age);
            oos.writeObject(user);

            //注册成功了
            File file = new File(staticDir, "/myweb/reg_success.html");
            response.setContentFile(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //3 给用户响应一个注册结果页面(注册成功或注册失败)


    }

    /**
     * 处理登录
     *
     * @param request
     * @param response
     */
    public void login(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null) {
            File file = new File(staticDir, "/myweb/login_input_error.html");
            response.setContentFile(file);
            return;
        }

        File userFile = new File(userDir, username + ".obj");
        if (userFile.exists()) {//文件存在，说明该用户存在
            try (
                    FileInputStream fis = new FileInputStream(userFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                //从文件中反序列化注册用户信息
                User user = (User) ois.readObject();
                //密码正确
                if (user.getPassword().equals(password)) {
                    File file = new File(staticDir, "/myweb/login_success.html");
                    response.setContentFile(file);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File file = new File(staticDir, "/myweb/login_fail.html");
        response.setContentFile(file);

    }

    /**
     * 用于显示用户列表的动态页面
     *
     * @param request
     * @param response
     */
    public void showAllUser(HttpServletRequest request, HttpServletResponse response) {
        //1 先用users目录里将所有的用户读取出来存入一个List集合备用
        List<User> userList = new ArrayList<>();
        /*
            首先获取users中所有名字以.obj结尾的子项 提示:userDir.listFiles()
            然后遍历每一个子项并用文件流连接对象输入流进行反序列化
            最后将反序列化的User对象存入userList集合
         */
        File[] subs = userDir.listFiles(f -> f.getName().endsWith(".obj"));
        for (File userFile : subs) {
            try (
                    FileInputStream fis = new FileInputStream(userFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                userList.add((User) ois.readObject());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //2 使用程序生成一个页面，同时遍历List集合将用户信息拼接到表格中
        PrintWriter pw = response.getWriter();
        pw.println("<!DOCTYPE html>");
        pw.println("<html lang=\"en\">");
        pw.println("<head>");
        pw.println("<meta charset=\"UTF-8\">");
        pw.println("<title>用户列表</title>");
        pw.println("</head>");
        pw.println("<body>");
        pw.println("<center>");
        pw.println("<h1>用户列表</h1>");
        pw.println("<table border=\"1\">");
        pw.println("<tr>");
        pw.println("<td>用户名</td>");
        pw.println("<td>密码</td>");
        pw.println("<td>昵称</td>");
        pw.println("<td>年龄</td>");
        pw.println("</tr>");
        for (User user : userList) {
            pw.println("<tr>");
            pw.println("<td>" + user.getUsername() + "</td>");
            pw.println("<td>" + user.getPassword() + "</td>");
            pw.println("<td>" + user.getNickname() + "</td>");
            pw.println("<td>" + user.getAge() + "</td>");
            pw.println("</tr>");
        }
        pw.println("</table>");
        pw.println("</center>");
        pw.println("</body>");
        pw.println("</html>");

        System.out.println("页面生成完毕!");


        //3 设置响应头Content-Type用于告知动态数据是什么
        response.setContentType("text/html");

    }
}





