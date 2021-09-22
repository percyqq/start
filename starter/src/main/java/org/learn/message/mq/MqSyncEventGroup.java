package org.learn.message.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.LinkedList;

/**
 * @date: 2019/12/06
 * @description: 具体消息事件
 */
@Slf4j
public class MqSyncEventGroup<T extends MqSyncMessage> extends MqSyncEvent {

    private LinkedList<MqSyncEvent> childrenEvents;

    public MqSyncEventGroup(ApplicationContext applicationContext, T msgObject) {
        super(applicationContext, msgObject);
        this.childrenEvents = new LinkedList<>();
    }

    @Override
    public void doAction(MqSyncMessage syncMessage) throws Exception {
        for (MqSyncEvent childrenEvent : childrenEvents) {
            childrenEvent.run();
        }
    }

    public final void addEvent(MqSyncEvent event) {
        childrenEvents.add(event);
    }

}
