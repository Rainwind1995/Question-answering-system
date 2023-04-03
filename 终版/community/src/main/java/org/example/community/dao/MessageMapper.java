package org.example.community.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.community.entity.Message;

import java.util.List;

@Mapper
public interface MessageMapper {

    //  针对每个会话列表返回一条最新的私信消息
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户会话数量
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 发送私信
    int insertMessage(Message message);

    // 修改消息的状态
    int updateStatus(List<Integer> ids, int status);

    // 下面的方法是自己添加的的
    List<Message> selectChatConversations(int userId);
    List<Message> selectChats(String conversationId);

    // 查询某个主题下最新通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题所包含的主题列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);



}
