package org.example.community.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.community.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {

    // 通过id查询用户
    User selectById(int id);

    // 通过username查询用户
    User selectByName(String username);

    // 通过email查询用户
    User selectByEmail(String email);

    // 查询所有用户
    int selectUserRows();

    // 分页查询所有用户
    List<User> selectAllUser(int offset, int limit);

    // 添加用户
    int insertUser(User user);

    // 更新用户状态
    int updateStatus(int id, int status);

    // 更新用户头像
    int updateHeader(int id, String headerUrl);

    // 更新密码
    int updatePassword(int id, String password);


}
