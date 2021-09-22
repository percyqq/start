package org.learn.message.mq.parse;

import com.alibaba.fastjson.JSON;
import org.learn.message.mq.MqSyncEvent;
import org.learn.message.mq.MqSyncEventGroup;
import org.learn.message.mq.MqSyncEventParser;
import org.learn.message.mq.MqSyncMessage;
import org.learn.message.mq.event.MqSyncDishBrandAddEvent;
import org.learn.message.mq.event.MqSyncDishShopGrantEvent;
import org.springframework.context.ApplicationContext;

import java.text.ParseException;

public class MqSyncDishBrandParser extends MqSyncEventParser<MqSyncEvent> {

    // 一组操作的消息  MqSyncEventParser
    @Override
    public MqSyncEvent parse(ApplicationContext applicationContext, JSON json) throws ParseException {
        MqSyncMessage mqSyncMessage = parseDefaultMessage(json);
        MqSyncEventGroup<MqSyncMessage> eventGroup = new MqSyncEventGroup<>(applicationContext, mqSyncMessage);

        MqSyncMessage message = JSON.parseObject(json.toJSONString(), MqSyncMessage.class);
        eventGroup.addEvent(new MqSyncDishBrandAddEvent(applicationContext, message));
        eventGroup.addEvent(new MqSyncDishShopGrantEvent(applicationContext, message));

        return eventGroup;
    }
}