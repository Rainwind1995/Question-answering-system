package org.example.community.entity;

import java.util.Date;

public class Comment {
    private int id;
    private int userId; // 用户id
    private int entityType; // 评论的类型(评论目标类),比如:1-帖子、2-评论、3-用户、4-课程、5-视频
    private int entityId; // 具体的评论id,比如具体是哪个id的帖子
    private int targetId; // 指向评论id,比如我评论了帖子,我当前这条评论可被其他人评论 我是A,其他人是B B->A
    private String content; // 评论内容
    private int status; // 评论状态 0-正常 1-禁言
    private Date createTime; // 回帖时间


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", targetId=" + targetId +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
