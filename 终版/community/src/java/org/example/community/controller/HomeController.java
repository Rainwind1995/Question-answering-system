package org.example.community.controller;

import org.example.community.entity.DiscussPost;
import org.example.community.entity.Page;
import org.example.community.entity.User;
import org.example.community.service.CollectService;
import org.example.community.service.DiscussPostService;
import org.example.community.service.LikeService;
import org.example.community.service.UserService;
import org.example.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private CollectService collectService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode){
        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        // 需要将 orderMode 带到分页路径上
        page.setPath("/index?orderMode=" + orderMode);
        // 我们这里先写死获取id为0的用户的所有帖子
        List<DiscussPost> list = discussPostService.findDiscussPost(0, page.getOffset(), page.getLimit(), orderMode);
        //我们获取的帖子需要和用户关联起来: DiscussPost 里面的userId 和 User 里面的id 关联
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        // 遍历list获取全部信息
        if(list != null){
            for(DiscussPost post : list){
                // 使用map存放关联关系
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                // 通过post对象获取User: 先获取post里面userId,然后通过userId获取User对象
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                // 获取点赞数
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                // 获取收藏数
                long collectCount = collectService.findCollectedCount(ENTITY_TYPE_POST, post.getId());
                map.put("collectCount", collectCount);
                discussPosts.add(map);
            }
        }


        // 将最终的结果放入model里面,然后传回页面
        model.addAttribute("discussPosts", discussPosts);
        // 将 orderMode 保存
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }


    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }


    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }

}
