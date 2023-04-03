package org.example.community.controller;


import org.example.community.entity.Page;
import org.example.community.entity.User;
import org.example.community.service.UserManagerService;
import org.example.community.service.UserService;
import org.example.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/admin/userManager")
public class UserManagerController {
    private static final Logger logger = LoggerFactory.getLogger(UserManagerController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserManagerService userManagerService;


    /**
     * 显示所有用户
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String getUserManagerPage(Model model, Page page){
        page.setRows(userService.findAllUserRows());
        // 这里的path是指我们这个Controller开头的那个(/admin/userManager) + 我们这个函数的path(/list)
        page.setPath("/admin/userManager/list");
        // 获取所有用户
        List<User> list = userService.findAllUser(page.getOffset(), page.getLimit());
        List<Map<String, Object>> all = new ArrayList<>();
        if(list != null){
            for(User user : list){
                Map<String, Object> map = new HashMap<>();
                map.put("user", user);
                all.add(map);
            }
        }
        // 将最终的结果放入model里面,然后传回页面
        model.addAttribute("all", all);

        return "/site/admin/usertable";
    }


    /**
     * 添加用户
     * @param username
     * @param password
     * @param email
     * @param type
     * @param status
     * @return
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String add(String username, String password, String email, int type, int status){

          int level = userManagerService.add(username, password, email, type, status);
          if(level == 1){
              return CommunityUtil.getJSONString(1, "请输入用户名!");
          }else if(level == 2){
              return CommunityUtil.getJSONString(2, "请输入密码!");
          }else if(level == 3){
              return CommunityUtil.getJSONString(3, "请输入邮箱!");
          }else if(level == 4){
              return CommunityUtil.getJSONString(4, "用户已存在,请重新输入用户名!");
          }else if(level == 5){
              return CommunityUtil.getJSONString(5, "邮箱已存在,请输入正确的邮箱!");
          }else{
              return CommunityUtil.getJSONString(0, "添加用户成功!");
          }

    }
}
