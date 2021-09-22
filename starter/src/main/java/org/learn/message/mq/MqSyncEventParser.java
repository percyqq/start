package org.learn.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.learn.message.mq.parse.MqSyncDishBrandParser;
import org.learn.message.mq.parse.MqSyncDishBrandTypeParser;
import org.springframework.context.ApplicationContext;

import java.text.ParseException;

/**
 * @author: xuwei01
 * @date: 2019/12/06
 * @description:
 */
public abstract class MqSyncEventParser<E extends MqSyncEvent> {

    static final MqSyncEventParser MqSyncDishBrandParser = new MqSyncDishBrandParser();
    static final MqSyncEventParser MqSyncDishBrandTypeParser = new MqSyncDishBrandTypeParser();
//    static final MqSyncEventParser MqSyncSkuPropertyEventParser = new MqSyncDishPropertyEventParser();
//    static final MqSyncEventParser MqSyncSkuPropertyTypeEventParser = new MqSyncDishPropertyTypeEventParser();
//    static final MqSyncEventParser MqSyncSkuUmEventParser = new MqSyncSkuUmEventParser();
//    static final MqSyncEventParser MqSyncDishBrandGrantParser = new MqSyncDishBrandGrantParser();

    public final E parse(ApplicationContext applicationContext, String topic, String msg) {
        MqSyncMessage mqSyncMessage = new MqSyncMessage(msg);
        mqSyncMessage.setTopic(topic);
        if (msg == null || msg.isEmpty()) {
            return (E) new MqSyncEvent.UnknownEvent(applicationContext, mqSyncMessage);
        }
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            jsonObject.put("topic", topic);
            return parse(applicationContext, jsonObject);
        } catch (Throwable t) {
            return (E) new MqSyncEvent.ParseErrorEvent(applicationContext, mqSyncMessage, t);
        }
    }

    public abstract E parse(ApplicationContext applicationContext, JSON json) throws ParseException;

    public MqSyncMessage parseDefaultMessage(JSON json) {
        MqSyncMessage mqSyncMessage = JSON.toJavaObject(json, MqSyncMessage.class);
        mqSyncMessage.setOriginMessage(json.toJSONString());
        return mqSyncMessage;
    }

}


