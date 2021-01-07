package org.learn.message.mq.event;

import org.learn.message.mq.MqSyncEvent;
import org.learn.message.mq.MqSyncMessage;
import org.springframework.context.ApplicationContext;

public class MqSyncDishShopGrantEvent extends MqSyncEvent<MqSyncMessage> {
    
    public MqSyncDishShopGrantEvent(ApplicationContext applicationContext, MqSyncMessage msgObject) {
        super(applicationContext, msgObject);
    }

    @Override
    public boolean doBefore(MqSyncMessage dishShopModifySyncMessage) {
        //DishBrandService dishBrandService = this.getService(DishBrandService.class);
        return true;//todo 业务check dishBrandService.isBrandInit(dishShopModifySyncMessage.getBrandIdenty());
    }
    
    @Override
    public void doAction(MqSyncMessage mqSyncMessage) throws Exception {
        
    }
}