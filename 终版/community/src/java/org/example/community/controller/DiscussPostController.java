package org.example.community.controller;

import org.apache.commons.lang3.StringUtils;
import org.example.community.entity.*;
import org.example.community.event.EventProducer;
import org.example.community.service.*;
import org.example.community.util.CommunityConstant;
import org.example.community.util.CommunityUtil;
import org.example.community.util.HostHolder;
import org.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CollectService collectService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;


//    @RequestMapping(path = "/add", method = RequestMethod.POST)
//    @ResponseBody
//    public String addDiscussPost(String title, String content) {
//        // 判断当前用户是否登录
//        User user = hostHolder.getUser();
//        if (user == null) {
//            return CommunityUtil.getJSONString(403, "请先登录!");
//        }
//
//        DiscussPost post = new DiscussPost();
//        // 过滤敏感词
//        post.setUserId(user.getId());
//        post.setTitle(title);
//        post.setContent(content);
//        post.setCreateTime(new Date());
//
//        discussPostService.addDiscussPost(post);
//
//        return CommunityUtil.getJSONString(0, "发布成功!");
//    }

    /**
     * 跳转到发布帖子页面
     *
     * @return
     */
    @RequestMapping(path = "/getPublishPage", method = RequestMethod.GET)
    public String getPublishPage() {
        return "/site/publish";
    }


    @RequestMapping(path = "/publish", method = RequestMethod.POST)
    public String publish(String title, String description, Model model) {
        // 获取用户
        User user = hostHolder.getUser();
        if(user == null){
            model.addAttribute("error", "用户未登陆");
            return "/site/publish";
        }

        model.addAttribute("title", title);
        model.addAttribute("description", description);

        //判断是否填入信息
        if (StringUtils.isBlank(title)) {
            model.addAttribute("error", "标题不能为空");
            return "/site/publish";
        }
        if (StringUtils.isBlank(description)) {
            model.addAttribute("error", "描述信息不能为空");
            return "/site/publish";
        }


        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title.trim());
        post.setContent(description);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());


        return "redirect:/index";
    }


    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDisPostById(discussPostId);
        model.addAttribute("post", post);
        // 用户
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("likeStatus", likeStatus);

        // 收藏数量
        long collectCount = collectService.findCollectedCount(ENTITY_TYPE_POST, post.getId());
        // 收藏状态
        int collectStatus =  hostHolder.getUser() == null ? 0 : collectService.hasCollected(hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId());
        model.addAttribute("collectCount",collectCount);
        model.addAttribute("collectStatus", collectStatus);


        // 评论分页信息
        page.setLimit(5); // 每页显示五条评论
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论列表: 给帖子的评论
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 回复: 给评论的评论
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        if(commentList != null){
            for(Comment comment : commentList){
                // 存放评论视图对象
                Map<String, Object> commentVo = new HashMap<>();
                // 一条评论
                commentVo.put("comment", comment);
                // 用户
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                commentVo.put("likeStatus", likeStatus);
                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(),0, Integer.MAX_VALUE);
                // 回复Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for(Comment replay : replyList){
                        Map<String, Object> replayVo = new HashMap<>();
                        // 回复
                        replayVo.put("replay", replay);
                        // 作者
                        replayVo.put("user", userService.findUserById(replay.getUserId()));
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, replay.getId());
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, replay.getId());
                        replayVo.put("likeCount", likeCount);
                        replayVo.put("likeStatus", likeStatus);
                        // 回复目标
                        User target = userService.findUserById(replay.getTargetId());
                        replayVo.put("target", target);

                        replyVoList.add(replayVo);
                    }
                }

                commentVo.put("replays", replyVoList);
                // 获取回复数量
                int replayCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replayCount", replayCount);
                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";

    }



    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        // 通过id获取帖子
        DiscussPost post = discussPostService.findDisPostById(id);
        // 获取帖子类型
        int type = post.getType()^1;
        // 1为置顶，0为正常， 1^1=0, 0^1=1
        discussPostService.updateType(id, type);
        // 将结果存入 map
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);


        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);


        return CommunityUtil.getJSONString(0, null, map);
    }

    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        // 通过id获取帖子
        DiscussPost post = discussPostService.findDisPostById(id);
        // 获取帖子状态
        int status = post.getStatus()^1;
        // 1为加精，0为正常， 1^1=0, 0^1=1
        discussPostService.updateStatus(id, status);
        // 将结果存入 map
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);


        return CommunityUtil.getJSONString(0, null, map);
    }


    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        // 2 为拉黑状态
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);


        return CommunityUtil.getJSONString(0);
    }





}
