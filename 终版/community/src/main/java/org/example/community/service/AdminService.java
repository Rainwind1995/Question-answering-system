package org.example.community.service;


import org.apache.commons.lang3.StringUtils;
import org.example.community.dao.AdminMapper;
import org.example.community.dao.UserMapper;
import org.example.community.entity.Admin;
import org.example.community.entity.User;
import org.example.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;




    // 登录
    public Map<String, Object> login(String username, String password){
        Map<String, Object> map = new HashMap<>();

        // 判断用户名是否输入
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "请输入账号!");
            return map;
        }

        // 判断密码是否为空
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "请输入密码!");
            return map;
        }

        // 判断用户是否存在
        Admin admin = adminMapper.selectByNameAndPwd(username, password);
        if(admin == null){
            map.put("usernameMsg", "用户不存在,请重新输入账号!");
            return map;
        }

        // 验证成功
        map.put("success", "登录成功");

        return map;
    }



}
