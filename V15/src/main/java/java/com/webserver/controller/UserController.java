package java.com.webserver.controller;

import com.webserver.core.DispatcherServlet;
import com.webserver.entity.User;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.*;
import java.net.URISyntaxException;

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
            staticDir = new File(root,"static");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        userDir = new File("./users");
        if(!userDir.exists()){
            userDir.mkdirs();
        }
    }

    public void reg(HttpServletRequest request, HttpServletResponse response){
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
        if(username==null||password==null||nickname==null||ageStr==null||
                !ageStr.matches("[0-9]+")){
            File file = new File(staticDir,"/myweb/reg_input_error.html");
            response.setContentFile(file);
            return;
        }
        int age = Integer.parseInt(ageStr);
        System.out.println(username+","+password+","+nickname+","+ageStr);


        //2 将用户信息保存
        File userFile = new File(userDir,username+".obj");
        /*
            判断是否为重复用户，若重复用户，则直接响应页面:have_user.html
            该页面剧中显示一行字:该用户已存在，请重新注册
         */
        if(userFile.exists()){//文件存在则说明是重复用户
            File file = new File(staticDir,"/myweb/have_user.html");
            response.setContentFile(file);
            return;
        }


        try (
                FileOutputStream fos = new FileOutputStream(userFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ){
            User user = new User(username,password,nickname,age);
            oos.writeObject(user);

            //注册成功了
            File file = new File(staticDir,"/myweb/reg_success.html");
            response.setContentFile(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //3 给用户响应一个注册结果页面(注册成功或注册失败)


    }
}





