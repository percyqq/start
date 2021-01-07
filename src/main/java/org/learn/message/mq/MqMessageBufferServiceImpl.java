package org.learn.message.mq;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

interface MqMessageBufferService {
    void bufferMessage(MqSyncMessage syncMessage);

    void clearBuffer(MqSyncMessage syncMessage);

    void recordError(String syncMessage, Throwable ex);

    void recordError(MqSyncMessage syncMessage, Exception ex);

    List<MqSyncMessage> getBufferMessage(String topic, Integer size);
}

/**
 * @date: 2019/12/06
 * @description:
 */
@Service
@Slf4j
public class MqMessageBufferServiceImpl implements MqMessageBufferService {

    private StringRedisTemplate redisTemplate;

    @Override
    public void bufferMessage(MqSyncMessage syncMessage) {
        String key = syncMessage.getMsgId();
        String json = syncMessage.getOriginMessage();
        Boolean exist = redisTemplate.opsForValue().setIfAbsent(key, json);
        if (exist == null || !exist) {
            log.warn("repeat sync msg, key : {}", key);
//            DishToScmSyncMessageBufferRecord recordDish = newDateMsg(syncMessage.getTopic());
//            syncMessage.setCurrentTime(recordDish.getServerUpdateTime());
//            recordDish.setMsgContent(json);
//            recordDish.setState(MsgBufferRecordDishState.Repeat.getState());//重复的消息
//            msgBufferRecordDishDao.insert(recordDish);
        }
    }

    @Override
    public void clearBuffer(MqSyncMessage syncMessage) {
        redisTemplate.delete(syncMessage.getMsgId());
    }

    @Override
    public void recordError(String syncMessage, Throwable ex) {
//        DishToScmSyncMessageBufferRecord recordDish = newDateMsg("json error");
//        recordDish.setMsgContent(syncMessage);
//        recordDish.setState(MsgBufferRecordDishState.Error.getState()); //解析出现异常
//        msgBufferRecordDishDao.insert(recordDish);
    }

    @Override
    public void recordError(MqSyncMessage syncMessage, Exception ex) {
//        DishToScmSyncMessageBufferRecord recordDish = newDateMsg(syncMessage.getTopic());
//        recordDish.setMsgContent(JSONObject.toJSONString(syncMessage));
//        if (ex instanceof RejectedExecutionException) {
//            //同步数据的任务没有被运行。
//            recordDish.setState(MsgBufferRecordDishState.Backlog.getState());//拒绝运行，则属于消息积压
//            msgBufferRecordDishDao.insert(recordDish);
//        } else {
//            recordDish.setState(MsgBufferRecordDishState.Exception.getState());//业务异常
//            msgBufferRecordDishDao.insert(recordDish);
//        }

        //执行同步任务出错后，也删除同步任务key
        redisTemplate.delete(syncMessage.getMsgId());
    }

    @Override
    public List<MqSyncMessage> getBufferMessage(String topic, Integer size) {
//        return msgBufferRecordDishDao.findByTopic(topic, Lists.newArrayList(MsgBufferRecordDishState.Backlog.getState(),
//                MsgBufferRecordDishState.Exception.getState()), size).stream().map(bo -> {
//            MqSyncMessage mqSyncMessage = new MqSyncMessage();
//            BeanUtils.copyProperties(bo, mqSyncMessage);
//            mqSyncMessage.setOriginMessage(bo.getMsgContent());
//            mqSyncMessage.setTopic(bo.getTopic());
//            mqSyncMessage.setMsgId(String.valueOf(bo.getId()));
//            return mqSyncMessage;
//        }).collect(Collectors.toList());

        return null;
    }

//    private DishToScmSyncMessageBufferRecord newDateMsg(String topic) {
//        DishToScmSyncMessageBufferRecord recordDish = new DishToScmSyncMessageBufferRecord();
//
//        recordDish.setMsgId(1L);
//        recordDish.setMsgType(MsgTypeEnum.DISH2SCM.getType());
//        recordDish.setTopic(topic);
//
//        Date time = new Date();
//        recordDish.setServerCreateTime(time);
//        recordDish.setServerUpdateTime(time);
//        return recordDish;
//    }
}
