package org.example.community;


import org.example.community.dao.MessageMapper;
import org.example.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MessageTest {
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testMessage(){
        //  针对每个会话列表返回一条最新的私信消息
        List<Message> list = messageMapper.selectConversations(111, 0, 10);
        for(Message m : list){
            System.out.println(m);
        }

        System.out.println("------------------------------------");

        // 查询当前用户会话数量
        int conversationCount = messageMapper.selectConversationCount(111);
        System.out.println(conversationCount);


        System.out.println("------------------------------------");
        // 查询某个会话所包含的私信列表
        List<Message> conversationList = messageMapper.selectLetters("111_112", 0, 10);
        for(Message m : conversationList){
            System.out.println(m);
        }

        System.out.println("------------------------------------");
        // 查询某个会话所包含的私信数量
        int letterCount = messageMapper.selectLetterCount("111_112");
        System.out.println(letterCount);


        System.out.println("------------------------------------");
        int unReadMessageCount = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(unReadMessageCount);

    }
}
