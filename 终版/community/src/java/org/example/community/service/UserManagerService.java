package org.example.community.service;

import org.apache.commons.lang3.StringUtils;
import org.example.community.dao.UserMapper;
import org.example.community.entity.User;
import org.example.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserManagerService {

    @Autowired
    private UserMapper userMapper;


    // 添加用户
    public int add(String username, String password, String email, int type, int status){

        // 判断用户名是否为空
        if(StringUtils.isBlank(username)){
            return 1;
        }

        // 判断密码是否为空
        if(StringUtils.isBlank(password)){
            return 2;
        }

        // 判断邮箱是否为空
        if(StringUtils.isBlank(email)){
            return 3;
        }

        // 注册之前需要判断当前用户是否已经存在
        User u = userMapper.selectByName(username);
        if(u != null){
            return 4;
        }

        // 验证邮箱是否存在
        if(userMapper.selectByEmail(email) != null){
            return 5;
        }

        // 注册用户
        User user = new User();

        // 设置用户名
        user.setUsername(username);
        // 生成5位随机字符串
        user.setSalt(CommunityUtil.generateUUID().substring(0 , 5));
        // 使用 md5 加密密码
        user.setPassword(CommunityUtil.md5(password) + user.getSalt());
        // 设置邮箱
        user.setEmail(email);
        // 设置用户类型
        user.setType(type);
        // 设置用户状态（0表示未激活）
        user.setStatus(status);
        // 设置头像路径(这里使用牛客网上的默认头像,牛客网上有1001个头像所以我们随机生成其中的一个)
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        // 设置注册时间
        user.setCreateTime(new Date());
        userMapper.insertUser(user);


        return 0;

    }
}
