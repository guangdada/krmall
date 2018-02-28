package com.platform.api;


import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.code.kaptcha.Producer;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.entity.UserVo;
import com.platform.utils.cache.CacheUtil;


/**
 * 验证码生成
 *
 * @author fengshuonan
 * @date 2017-05-05 23:10
 */
@Controller
@RequestMapping("/api/imgcode")
public class ApiImgCodeController {

    @Autowired
    Producer producer;

    /**
     * 生成验证码
     * @Title: index   
     * @param request
     * @param response
     * @param sessionid
     * @date:   2017年11月13日 上午11:11:17 
     * @author: chengxg
     */
    @RequestMapping("/register")
    public void register(@LoginUser UserVo loginUser, HttpServletResponse response) {
        response.setDateHeader("Expires", 0);

        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");

        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");

        // return a jpeg
        response.setContentType("image/jpeg");

        // create the text for the image
        String capText = producer.createText();

        // store the text in the session
        //session.setAttribute(Constants.KAPTCHA_SESSION_KEY, capText);
        CacheUtil.put(CacheUtil.registerImgCode, loginUser.getWeixin_openid(), capText);

        // create the image with the text
        BufferedImage bi = producer.createImage(capText);
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write the data out
        try {
            ImageIO.write(bi, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            try {
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

