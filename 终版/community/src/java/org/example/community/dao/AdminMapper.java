package org.example.community.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.community.entity.Admin;

@Mapper
public interface AdminMapper {

    // 通过name和pass查询用户
    Admin selectByNameAndPwd(String username, String password);
}
