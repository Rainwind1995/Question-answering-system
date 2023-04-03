package org.example.community.controller;


import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.example.community.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


@Controller
@RequestMapping(path = "/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private Producer captchaProducer;


    /**
     * 跳转到管理员登录界面
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/admin/login";

    }


    /**
     * 添加用户页面
     * @return
     */
    @RequestMapping(path = "/add")
    public String addUserPage(){
        return "/site/admin/add-user";
    }


    /**
     * 跳转到管理员首页
     * @return
     */
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(){
        return "/site/admin/index";
    }


    /**
     * 管理员系统首页
     * @return
     */
    @RequestMapping(path = "/main", method = RequestMethod.GET)
    public String getMainPage(){
        return "/site/admin/lyearmain";
    }


    /**
     * 用户管理页面
     * @return
     */
    @RequestMapping(path = "/userManagement", method = RequestMethod.GET)
    public String userManagementPage(){
        return "/site/admin/usertable";
    }


    /**
     * 退出
     * @return
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(){
        return "redirect:/admin/login";
    }



    @RequestMapping(path = "/captcha", method = RequestMethod.GET)
    public void getCaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = captchaProducer.createText();
        BufferedImage image = captchaProducer.createImage(text);

        // 将验证码存入session
        session.setAttribute("captcha", text);

        // 将图片输入到浏览器
        response.setContentType("image/png");

        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }


    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String captcha, Model model, HttpSession session){
        // 获取生成的验证码
        String code = (String) session.getAttribute("captcha");

        // 如果验证码为null,直接重新登录
        if(StringUtils.isBlank(captcha) || StringUtils.isBlank(code) || !captcha.equalsIgnoreCase(code)){
            model.addAttribute("captchaMsg", "验证码不正确");
            return "/site/admin/login";
        }

        // 判断用户是否存在
        Map<String, Object> map = adminService.login(username, password);
        if(map.containsKey("success")){
            model.addAttribute("successMsg", map.get("success"));
            return "redirect:/admin/index";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/admin/login";
        }

    }




}
