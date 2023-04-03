package org.example.community.controller;

import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.example.community.entity.User;
import org.example.community.service.UserService;
import org.example.community.util.CommunityConstant;
import org.example.community.util.CommunityUtil;
import org.example.community.util.RedisKeyUtil;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            // 点击跳转的目标链接
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            // 如果注册失败,可能是下面三种状态,所以我们直接列举出来
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) { // 激活成功
            model.addAttribute("msg", "激活成功,您的账号可以正常使用了!");
            model.addAttribute("target", "/login"); // 跳转到登录界面
        } else if (result == ACTIVATION_REPEAT) { // 重复激活
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index"); // 返回首页
        } else { // 激活失败
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }


//    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
//    public void getKaptcha(HttpServletResponse response, HttpSession session) {
//        // 生成验证码
//        String text = kaptchaProducer.createText();
//        BufferedImage image = kaptchaProducer.createImage(text);
//
//        // 将验证码存入session
//        session.setAttribute("kaptcha", text);
//
//        // 将图片输入到浏览器
//        response.setContentType("image/png");
//
//        try {
//            OutputStream os = response.getOutputStream();
//            ImageIO.write(image, "png", os);
//        } catch (IOException e) {
//            logger.error("响应验证码失败" + e.getMessage());
//        }
//    }



    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 设置验证码归属者
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie= new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入到 redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输入到浏览器
        response.setContentType("image/png");

        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }


    /**
     *
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param rememberme 是否记住状态
     * @param model
     * @param response
     * @return
     */
//    @RequestMapping(path = "/login", method = RequestMethod.POST)
//    public String login(String username, String password, String code, Boolean rememberme, Model model,
//                        HttpSession session, HttpServletResponse response) {
//        // 校验验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");  // 获取验证码
//        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
//            model.addAttribute("codeMsg", "验证码不正确");
//            return "/site/login";
//        }
//
//
//        // 如果我们没有勾选 rememberme 那么登录的时候会返回的是一个 null, 下面执行的时候会报空指针异常,所以需要做一个判断
//        if(rememberme == null){
//            rememberme = false;
//        }
//
//        // 检验账号密码
//        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
//        Map<String, Object> map = userService.login(username, password, expiredSeconds);
//        if(map.containsKey("ticket")){
//            // 新建一个cookie:将ticket保存到本地,map.get("ticket").toString()是因为map.get("ticket") 是Object所以需要转为String
//            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
//            // 设置cookie的有效路径,我们设置整个项目都生效
//            cookie.setPath(contextPath);
//            // 设置cookie的有效时间
//            cookie.setMaxAge(expiredSeconds);
//            // 将cookie发送给页面
//            response.addCookie(cookie);
//
//            return "redirect:/index";
//        }else{
//            model.addAttribute("usernameMsg", map.get("usernameMsg"));
//            model.addAttribute("passwordMsg", map.get("passwordMsg"));
//
//            return "/site/login";
//        }
//    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, Boolean rememberme, Model model,
                         @CookieValue("kaptchaOwner") String kaptchaOwner, HttpServletResponse response) {
        // 校验验证码
        String kaptcha = null;
        // 判断验证码归属者是否为空
        if(StringUtils.isNoneBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }


        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }


        // 如果我们没有勾选 rememberme 那么登录的时候会返回的是一个 null, 下面执行的时候会报空指针异常,所以需要做一个判断
        if(rememberme == null){
            rememberme = false;
        }

        // 检验账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            // 新建一个cookie:将ticket保存到本地,map.get("ticket").toString()是因为map.get("ticket") 是Object所以需要转为String
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            // 设置cookie的有效路径,我们设置整个项目都生效
            cookie.setPath(contextPath);
            // 设置cookie的有效时间
            cookie.setMaxAge(expiredSeconds);
            // 将cookie发送给页面
            response.addCookie(cookie);

            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));

            return "/site/login";
        }
    }


    /**
     * @CookieValue("ticket"):这样写可以直接获取到存在cookiei里面的ticket的值
     * @param ticket
     * @return
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }


    /**
     * 跳转到忘记密码界面
     * @return
     */
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage(){
        return "/site/forget";
    }


}
