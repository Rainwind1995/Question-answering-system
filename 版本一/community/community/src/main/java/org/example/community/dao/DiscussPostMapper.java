package org.example.community.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.community.entity.DiscussPost;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 查询所有评论,这里需要加上分页
    // offset:每一页的起始行号、limit: 每一页限制的条数
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // 查询评论的总条数
    // @Param注解用于给参数取别名,如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

}
