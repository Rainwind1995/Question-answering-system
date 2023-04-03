package org.example.community.controller;

import com.google.code.kaptcha.Producer;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.example.community.annotaion.LoginRequired;
import org.example.community.entity.Comment;
import org.example.community.entity.DiscussPost;
import org.example.community.entity.Page;
import org.example.community.entity.User;
import org.example.community.service.*;
import org.example.community.util.CommunityConstant;
import org.example.community.util.CommunityUtil;
import org.example.community.util.HostHolder;
import org.example.community.util.MailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private Producer kaptchaProduce;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;


    /**
     * @return 跳转设置页面
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 文件上传名称
        String fileName = CommunityUtil.generateUUID();
        System.out.println(fileName);
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "/site/setting";
    }

    // 更新头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1, "文件名字不能为空");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }




    /**
     * 上传头像
     *
     * @param headerImage 文件的上传一般需要使用 MultipartFile
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        // 判断上传文件是否为null
        if (headerImage == null) {
            model.addAttribute("error", "文件不能为空, 请选择图片!");
            return "/site/setting";
        }

        // 获取上传文件名
        String fileName = headerImage.getOriginalFilename();
        // 获取文件的类型, 比如: xx.png 我们需要获取的就是png
        // lastIndexOf("."):是指获取该文件最后一次出现的 . 的索引,+1是因为substring()是左闭右开
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

        // 判断后缀名是否为空
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式名不正确");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + "." + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        // 上传头像的文件夹
        File filePath = new File(uploadPath);
        // 我们这里先判断上传图片的文件夹是否存在,不存在就创建存放头像的文件夹
        if (!filePath.isDirectory() && !filePath.exists()) {
            filePath.mkdir();
        }

        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败", e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常", e);
        }

        // 更新当前用户头像路径
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        // 将头像路径添加到服务器(这里是项目的static下)
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }


    /**
     * 获取头像
     *
     * @param fileName
     * @param response
     */
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 获取文件存放的路径
        fileName = uploadPath + "/" + fileName;
        // 获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 响应图片
        // response.setContentType(MIME)的作用是使客户端浏览器，区分不同种类的数据，并根据不同的MIME调用浏览器内不同的程序嵌入模块来处理相应的数据。
        response.setContentType("image/" + suffix);

        try (
                // 因为我们的图片是以二进制形式传输的,所以我们需要通过字节流来读取
                FileInputStream fis = new FileInputStream(fileName);
                // 读取字节流
                OutputStream os = response.getOutputStream();
        ) {
            // 每次读取的字节数
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取头像失败: " + e.getMessage());
        }
    }


    /**
     * 修改密码
     *
     * @param newPassword
     * @param olderPassWord
     * @param confirmPassword
     * @param model
     * @return
     */
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(@CookieValue("ticket") String ticket, String olderPassWord, String newPassword, String confirmPassword, Model model) {
        // 获取当前用户
        User user = hostHolder.getUser();
        // 获取原密码
        olderPassWord = CommunityUtil.md5(olderPassWord) + user.getSalt();
        if (!olderPassWord.equals(user.getPassword())) {
            model.addAttribute("olderPasswordMsg", "原始密码输入不正确");
            return "/site/setting";
        }

        // 判断两次输入得密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordMsg", "两次输入的密码不一致,请重新输入!");
            return "/site/setting";
        }

        // 更新数据库
        newPassword = CommunityUtil.md5(newPassword) + user.getSalt();
        userService.updatePassword(user.getId(), newPassword);

        // 密码修改成功退出登录
        userService.logout(ticket);
        model.addAttribute("target", "/index");
        model.addAttribute("msg", "密码修改成功");
        // 跳转到提示界面
        return "/site/operate-result";
    }


    /**
     * 重置密码
     *
     * @param email
     * @param password
     * @param code
     * @param model
     * @return
     */
    @RequestMapping(path = "/resetPwd", method = RequestMethod.POST)
    public String resetPassword(String email, String password, String code, Model model, HttpSession session) {

        // 获取验证码
        String getCode = (String) session.getAttribute("code");

        // 判断是否收到验证码
        if (getCode == null) {
            model.addAttribute("codeMsg", "请发送验证码!");
            return "/site/forget";
        }
        // 判断输入的邮箱是否为空
        if (StringUtils.isBlank(email)) {
            model.addAttribute("emailMsg", "请输入邮箱!");
            return "/site/forget";
        }
        // 判断输入的邮箱是否存在
        User user = userService.findUserByEmail(email);
        if (user == null) {
            model.addAttribute("emailMsg", "您输入的邮箱不存在!");
            return "/site/forget";
        }


        // 判断输入的验证码是否正确
        if (code.equals(getCode)) {
            userService.updatePassword(user.getId(), CommunityUtil.md5(password) + user.getSalt());
            model.addAttribute("msg", "密码修改成功");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("codeMsg", "验证码输入错误");
            return "/site/forget";
        }

        return "/site/operate-result";
    }

    /**
     * 发送验证码
     *
     * @param email
     * @param session
     * @return
     * @ResponseBody:将java对象转为json格式的数据。因为我们返回给前端的数据是json格式
     */
    @RequestMapping(path = "/sendResetPwdKatcha", method = RequestMethod.POST)
    @ResponseBody
    public String sendResetPasswordKaptcha(String email, HttpSession session) {
        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", email);
        // 生成验证码
        String code = kaptchaProduce.createText();

        context.setVariable("code", code);
        session.setAttribute("code", code);
        // 设置session过期时间为2分钟
        session.setMaxInactiveInterval(2 * 60);

        // 发送内容
        String content = templateEngine.process("/mail/forget", context);
        // 发送邮件
        mailClient.sendMail(email, "忘记密码", content);

        // 返回json格式: {"code":0}
        return CommunityUtil.getJSONString(0);
    }


    /**
     * 个人主页
     *
     * @param userId
     * @param model
     * @return
     */
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("user", user);
        // 获取点赞数
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 获取关注数
        long followeeCount = followService.findFolloweeCount(user.getId(), ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 获取粉丝数
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, user.getId());
        model.addAttribute("followerCount", followerCount);
        // 是否关注
        boolean hasFollowed = false;
        // 判断当前用户是否已关注
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, user.getId());
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }


    @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Model model, Page page) {
        // 获取当前用户
        User user = userService.findUserById(userId);

        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("user", user);

        int postCount = discussPostService.findPostCountById(userId);
        model.addAttribute("postCount", postCount);

        // 设置分页
        page.setLimit(5);
        page.setPath("/user/mypost/" + userId);
        page.setRows(postCount);
        // 查询当前用户发的帖子
        List<DiscussPost> posts = discussPostService.findPostById(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> postList = new ArrayList<>();
        if (posts != null) {
            for (DiscussPost post : posts) {
                Map<String, Object> map = new HashMap<>();
                // 存入单个帖子
                map.put("post", post);
                // 获取单个帖子获取的点赞数
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                postList.add(map);
            }
        }

        model.addAttribute("postList", postList);


        return "/site/my-post";
    }


    @RequestMapping(path = "/myReply/{userId}", method = RequestMethod.GET)
    public String getMyRepley(@PathVariable("userId") int userId, Page page, Model model) {
        // 获取当前用户
        User user = userService.findUserById(userId);

        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("user", user);

        int commentCount = commentService.findCommentCountById(userId);
        model.addAttribute("commentCount", commentCount);

        page.setLimit(5);
        page.setPath("/user/myReply/" + userId);
        page.setRows(commentCount);

        List<Comment> comments = commentService.findComment(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentList = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                DiscussPost post = discussPostService.findDisPostById(comment.getEntityId());
                map.put("post", post);

                commentList.add(map);
            }
        }

        model.addAttribute("commentList", commentList);
        return "/site/my-reply";
    }


}
