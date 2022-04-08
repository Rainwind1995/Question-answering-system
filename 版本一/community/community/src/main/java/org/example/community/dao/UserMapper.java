package org.example.community.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.community.entity.User;

@Mapper
public interface UserMapper {

    // 通过id查询用户
    User selectById(int id);

}
