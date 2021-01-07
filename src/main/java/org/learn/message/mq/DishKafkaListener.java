package org.learn.message.mq;

import lombok.extern.slf4j.Slf4j;
import org.learn.message.mq.parse.MqSyncDishBrandParser;
import org.learn.message.mq.parse.MqSyncDishBrandTypeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;


import static org.learn.message.mq.MqSyncEventParser.MqSyncDishBrandParser;

/**
 * @author: xuwei01
 * @date: 2019/12/06
 * @description:
 */
@Slf4j
@Component
public class DishKafkaListener {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MqMessageBufferService mqMessageBufferService;

    @Qualifier("mqExecutorService")
    @Autowired
    ExecutorService mqExecutorService;

    @Value("${kafka.queue.sku.topic}")
    private String topicSku;

    @Value("${kafka.queue.skuProperty.topic}")
    private String topicSkuProperty;

    @Value("${kafka.queue.skuPropertyType.topic}")
    private String topicSkuPropertyType;

    @Value("${kafka.queue.skuType.topic}")
    private String topicSkuType;

    @Value("${kafka.queue.skuUm.topic}")
    private String topicSkuUm;

    @Value("${kafka.queue.skuGrant.topic}")
    private String topicSkuGrant;


    @KafkaListener(groupId = "${kafka.queue.sku.group}", topics = "${kafka.queue.sku.topic}")
    public void consumerSku(String message) {
        printLog("Sku-" + topicSku, message);
        MqSyncEvent<MqSyncMessage> mqSyncEvent = MqSyncDishBrandParser.parse(applicationContext, topicSku, message);
        registerCallbackAndDoBuffer(mqSyncEvent);
    }

    @KafkaListener(groupId = "${kafka.queue.skuType.group}", topics = "${kafka.queue.skuType.topic}")
    public void consumerSkuType(String message) {
        printLog("SkuType-" + topicSkuType, message);
        MqSyncEvent<MqSyncMessage> mqSyncEvent = MqSyncEventParser.MqSyncDishBrandTypeParser.parse(applicationContext, topicSkuType, message);
        registerCallbackAndDoBuffer(mqSyncEvent);
    }

//    @KafkaListener(groupId = "${kafka.queue.skuProperty.group}", topics = "${kafka.queue.skuProperty.topic}")
//    public void consumerSkuProperty(String message) {
//        printLog("SkuProperty-" + topicSkuProperty, message);
//        MqSyncEvent<MqSyncMessage> mqSyncEvent = MqSyncSkuPropertyEventParser.parse(applicationContext, topicSkuProperty, message);
//        registerCallbackAndDoBuffer(mqSyncEvent);
//    }
//
//    @KafkaListener(groupId = "${kafka.queue.skuPropertyType.group}", topics = "${kafka.queue.skuPropertyType.topic}")
//    public void consumerSkuPropertyType(String message) {
//        printLog("SkuPropertyType-" + topicSkuPropertyType, message);
//        MqSyncEvent<MqSyncMessage> mqSyncEvent = MqSyncSkuPropertyTypeEventParser.parse(applicationContext, topicSkuPropertyType, message);
//        registerCallbackAndDoBuffer(mqSyncEvent);
//    }
//
//    @KafkaListener(groupId = "${kafka.queue.skuUm.group}", topics = "${kafka.queue.skuUm.topic}")
//    public void consumerSkuUm(String message) {
//        printLog("SkuUm-" + topicSkuUm, message);
//        MqSyncEvent<MqSyncMessage> mqSyncEvent = MqSyncSkuUmEventParser.parse(applicationContext, topicSkuUm, message);
//        registerCallbackAndDoBuffer(mqSyncEvent);
//    }
//
//    @KafkaListener(groupId = "${kafka.queue.skuGrant.group}", topics = "${kafka.queue.skuGrant.topic}")
//    public void consumerSkuGrant(String message) {
//        printLog("SkuGrant-" + topicSkuGrant, message);
//        MqSyncEvent<MqSyncMessage> mqSyncEvent = MqSyncDishBrandGrantParser.parse(applicationContext, topicSkuGrant, message);
//        registerCallbackAndDoBuffer(mqSyncEvent);
//    }

    private void printLog(String topic, String message) {
        log.info("Receive Message 【{}】 >> {}", topic, message);
    }

    private void registerCallbackAndDoBuffer(MqSyncEvent event) {

        // 进行黑名单逻辑处理
//        if (nameListService.isBlackList(event.getMsgObject().getBrandIdenty())) {
//            log.info("丢弃消息         品牌：{} 在黑名单 不同步商品及物品数据 消息内容: {}", event.getMsgObject().getBrandIdenty(), event.getMsgObject().getOriginMessage());
//            return;
//        }

        event.registerCallback(
                syncMessage -> mqMessageBufferService.bufferMessage(syncMessage),
                (syncMessage, error) -> {
                    if (error != null) {
                        log.error("Receive Message 【{}】 >> {} \n Error >> \n", syncMessage.getTopic(), syncMessage.getOriginMessage(), error);
                        mqMessageBufferService.recordError(syncMessage, error);

                    }
                    mqMessageBufferService.clearBuffer(syncMessage);
                }
        );
        if (event instanceof MqSyncEvent.UnknownEvent) {
            event.run();
        } else if (event instanceof MqSyncEvent.ParseErrorEvent) {
            MqSyncEvent.ParseErrorEvent errorEvent = (MqSyncEvent.ParseErrorEvent) event;
            log.error("Receive Message 【{}】 >> {} \n Error >> \n", event.getMsgObject().getTopic(),
                    event.getMsgObject().getOriginMessage(), errorEvent.getThrowable());
            mqMessageBufferService.recordError(errorEvent.getMsgObject().getOriginMessage(), errorEvent.getThrowable());
        } else {
            try {
                mqExecutorService.submit(event);
            } catch (RejectedExecutionException ree) {
                log.error("Receive Message 【{}】 >> {} \n Error >> \n", event.getMsgObject().getTopic(),
                        event.getMsgObject().getOriginMessage(), ree);
                mqMessageBufferService.recordError(event.getMsgObject(), ree);
            }
        }
    }

}
