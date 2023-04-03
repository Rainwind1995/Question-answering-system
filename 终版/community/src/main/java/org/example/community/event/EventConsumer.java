package org.example.community.event;


import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.community.entity.DiscussPost;
import org.example.community.entity.Event;
import org.example.community.entity.Message;
import org.example.community.service.DiscussPostService;
import org.example.community.service.ElasticsearchService;
import org.example.community.service.MessageService;
import org.example.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息内容为空");
            return ;
        }

        // 对象不为空 我们将接收的消息(json 类型) 转为相应的对象类型
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式不正确");
            return ;
        }

        // 发送站内通知
        Message message = new Message();
        // 默认系统发送消息
        message.setFromId(SYSTEM_USER_ID);
        // 接收消息用户id
        message.setToId(event.getEntityUserId());
        // 会话id: 这里我们用 topic 来标志
        message.setConversationId(event.getTopic());
        // 消息发送时间
        message.setCreateTime(new Date());
        // 发送内容
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        // 一些 message 表里面不包含的字段 我们需要另外存储
        if(!event.getData().isEmpty()){
            for(Map.Entry<String, Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);

    }


    // 消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息内容为空");
            return ;
        }

        // 对象不为空 我们将接收的消息(json 类型) 转为相应的对象类型
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式不正确");
            return ;
        }

        // 触发发帖事件需要将帖子保存到 Elasticsearch 服务器上
        DiscussPost post = discussPostService.findDisPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);

    }


    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息内容为空");
            return ;
        }

        // 对象不为空 我们将接收的消息(json 类型) 转为相应的对象类型
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式不正确");
            return ;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());

    }

}
