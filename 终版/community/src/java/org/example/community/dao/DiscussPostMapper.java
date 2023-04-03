package org.example.community.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.community.entity.DiscussPost;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 查询所有评论,这里需要加上分页
    // offset:每一页的起始行号、limit: 每一页限制的条数
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    // 查询评论的总条数
    // @Param注解用于给参数取别名,如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);


    // 发布帖子
    int insertDiscussPost(DiscussPost discussPost);

    // 通过id查询帖子
    DiscussPost selectDiscussPostById(int id);

    // 更新评论数量
    int updateCommentCount(int id, int commentCount);

    // 修改帖子类型(置顶)
    int updateType(int id, int type);

    // 修改帖子状态 (加精、删除)
    int updateStatus(int id, int status);

    // 更新帖子分数
    int updateScore(int id, double score);

    // 获取用户发布帖子数量
    int selectPostCountById(int userId);

    // 获取用户发的帖子
    List<DiscussPost> selectPostById(int userId, int offset, int limit);


}
