package com.it;


import org.apache.wicket.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;


@Controller
public class Test {
    static int i=0;


    @RequestMapping(value = "hello",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String test(HttpServletRequest request, HttpSession session,HttpServletResponse response) throws Exception {
        String str=null;

        System.out.println("执行了");
        MultipartHttpServletRequest multipartRequest=(MultipartHttpServletRequest) request;

        System.out.println("执行了");
        MultipartFile multipartFile = multipartRequest.getFile("wavData");

        //file是form-data中二进制字段对应的name

        System.out.println("执行了");
        System.out.println(multipartFile.getSize());
        File localFile = new File("D:\\"+(i++)+".mp3");

        System.out.println("执行了");
        multipartFile.transferTo(localFile);

        System.out.println("执行了");
        ShiBieWav2 sbw2=new ShiBieWav2();
        str = sbw2.shiBieWenJianLiu();

        System.out.println(str+"2");
        return str;
    }


}
