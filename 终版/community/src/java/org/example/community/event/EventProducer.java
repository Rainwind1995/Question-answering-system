package org.example.community.event;

import com.alibaba.fastjson.JSONObject;
import org.example.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event){
        // 发布消息
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
