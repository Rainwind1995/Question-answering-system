package org.example.community.service;

import org.example.community.dao.UserMapper;
import org.example.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    // 通过id查找用户
    public User findUserById(int id){
        return userMapper.selectById(id);
    }


}
