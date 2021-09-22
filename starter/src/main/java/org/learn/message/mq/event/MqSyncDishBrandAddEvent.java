package org.learn.message.mq.event;

import org.learn.concurrent.ConcurrentSplitter;
import org.learn.message.mq.MqSyncEvent;
import org.learn.message.mq.MqSyncMessage;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class MqSyncDishBrandAddEvent extends MqSyncEvent<MqSyncMessage> {

    public MqSyncDishBrandAddEvent(ApplicationContext applicationContext, MqSyncMessage msgObject) {
        super(applicationContext, msgObject);
    }

    @Override
    public void doAction(MqSyncMessage mqSyncMessage) throws Exception {
        //DishService dishService = this.getService(DishService.class);
        //同步物品
        List<Long> dishIds = mqSyncMessage.getDishBrandIds();

        ConcurrentSplitter.split(dishIds, 500, new ConcurrentSplitter.SplitRunnable<Long>() {
            @Override
            public void run(List<Long> group) {
//                List<DishVo> dishs = dishService.getBrandDishBusiDatasByIds(mqSyncMessage.getBrandIdenty(), group);
//                // 处理dish_brand表数据
//                dishBrandService.insertDishBrandOrUpdate(dishs);
            }
        });
    }
}