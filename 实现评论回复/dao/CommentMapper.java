package org.example.community.dao;


import org.apache.ibatis.annotations.Mapper;
import org.example.community.entity.Comment;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 通过帖子类型查找所有评论
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查询评论数
    int selectCountByEntity(int entityType, int entityId);

    // 添加评论
    int insertComment(Comment comment);


}
