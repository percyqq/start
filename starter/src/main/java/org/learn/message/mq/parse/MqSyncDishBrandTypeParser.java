package org.learn.message.mq.parse;

import com.alibaba.fastjson.JSON;
import org.learn.message.mq.MqSyncEvent;
import org.learn.message.mq.MqSyncEventParser;
import org.learn.message.mq.MqSyncMessage;
import org.learn.message.mq.event.MqSyncDishBrandAddEvent;
import org.learn.message.mq.event.MqSyncDishShopGrantEvent;
import org.springframework.context.ApplicationContext;

import java.text.ParseException;

public class MqSyncDishBrandTypeParser extends MqSyncEventParser<MqSyncEvent> {

    // 单个操作的消息  MqSyncEventGroup
    @Override
    public MqSyncEvent parse(ApplicationContext applicationContext, JSON json) throws ParseException {
        MqSyncMessage mqSyncMessage = parseDefaultMessage(json);
        int type = mqSyncMessage.getOperation();
        MqSyncMessage message = JSON.parseObject(json.toJSONString(), MqSyncMessage.class);
        message.setOriginMessage(json.toJSONString());
        if (OtQueue.SkuType.ADD.equalsType(type)) {
            return new MqSyncDishBrandAddEvent(applicationContext, message);
        } else if (OtQueue.SkuType.EDIT.equalsType(type)) {
            return new MqSyncDishShopGrantEvent(applicationContext, message);
        }
        return new MqSyncEvent.UnknownEvent(applicationContext, mqSyncMessage);
    }
}