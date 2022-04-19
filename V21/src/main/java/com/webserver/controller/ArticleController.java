package com.webserver.controller;

import com.webserver.core.DispatcherServlet;
import com.webserver.entity.Article;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;

/**
 * 处理与文章相关的业务
 */
public class ArticleController {
    private static File articleDir;//存放所有文章的目录
    private static File root;
    private static File staticDir;
    static {
        try {
            root = new File(DispatcherServlet.class.getClassLoader().getResource(".").toURI());
            staticDir = new File(root,"static");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        articleDir = new File("./articles");
        if(!articleDir.exists()){
            articleDir.mkdirs();
        }
    }


    public void writeArticle(HttpServletRequest request, HttpServletResponse response){
        //1获取表单数据
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String content = request.getParameter("content");

        if(title==null||author==null||content==null){
            File file = new File(staticDir,"/myweb/article_fail.html");
            response.setContentFile(file);
            return;
        }

        //保存文章
        File articleFile = new File(articleDir,title+".obj");

        if(articleFile.exists()){//文件存在则说明是重复文章
            File file = new File(staticDir,"/myweb/article_fail.html");
            response.setContentFile(file);
            return;
        }
        //将文章序列化到文件里
        try(
            FileOutputStream fos = new FileOutputStream(articleFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
        ){
            Article article = new Article(title,author,content);
            oos.writeObject(article);

            File file = new File(staticDir,"/myweb/article_success.html");
            response.setContentFile(file);
        }catch(Exception e){
            e.printStackTrace();
        }


    }
}
