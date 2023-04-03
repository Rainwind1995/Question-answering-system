package org.example.community.controller;

import org.example.community.entity.Page;
import org.example.community.entity.User;
import org.example.community.service.CollectService;
import org.example.community.service.UserService;
import org.example.community.util.CommunityConstant;
import org.example.community.util.CommunityUtil;
import org.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CollectController implements CommunityConstant {
    @Autowired
    private CollectService collectService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    @RequestMapping(path = "/collect", method = RequestMethod.POST)
    @ResponseBody
    public String collect(int entityType, int entityId) {
        // 获取当前用户
        User user = hostHolder.getUser();
        // 收藏
        collectService.collect(user.getId(), entityType, entityId);
        // 获取单个实体收藏的数量
        long collectCount = collectService.findCollectedCount(entityType, entityId);
        // 获取当前实体的收藏状态
        int collectStatus = collectService.hasCollected(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("collectCount", collectCount);
        map.put("collectStatus", collectStatus);


        return CommunityUtil.getJSONString(0, null, map);
    }

    @RequestMapping(path = "/collect/{userId}", method = RequestMethod.GET)
    public String getCollect(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/collect/" + userId);
        page.setRows((int) collectService.findCollectCount(userId, ENTITY_TYPE_POST));

        long collectCount = collectService.findCollectCount(userId, ENTITY_TYPE_POST);
        model.addAttribute("collectCount", collectCount);

        List<Map<String, Object>> collectList = collectService.findCollect(userId, page.getOffset(), page.getLimit());

        model.addAttribute("collectList",collectList);

        return "/site/my-collect";
    }


}
