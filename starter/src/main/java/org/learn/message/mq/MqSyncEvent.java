package org.learn.message.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

/**
 * @date: 2019/12/06
 * @description: 具体消息事件
 */
@Slf4j
public abstract class MqSyncEvent<T extends MqSyncMessage> implements Runnable {

    private ApplicationContext applicationContext;

    private T msgObject;

    private Before<T> before;

    private After<T> after;

    protected MqSyncEvent(ApplicationContext applicationContext, T msgObject) {
        this.applicationContext = applicationContext;
        this.msgObject = msgObject;
    }

    public boolean doBefore(T t) {
        if (before != null) {
            before.before(t);
        }
        return true;
    }

    @Override
    public final void run() {
        try {
            RequestContext.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
            boolean needDoNext = doBefore(msgObject);
            // 前置拦截完成之后可以不执行后续逻辑
            if (needDoNext) {
                doAction(msgObject);
            }
            doAfter(msgObject, null);
        } catch (Exception e) {
            doAfter(msgObject, e);
        } finally {
            RequestContext.remove();
        }
    }

    public void doAfter(T t, Exception e) {
        if (after != null) {
            after.after(t, e);
        }
    }

    public abstract void doAction(T t) throws Exception;

    public final void registerCallback(Before<T> before, After<T> after) {
        this.before = before;
        this.after = after;
    }

    public final <S> S getService(Class<S> serviceClass) {
        return applicationContext.getBean(serviceClass);
    }

    public T getMsgObject() {
        return msgObject;
    }

    public interface Before<T extends MqSyncMessage> {
        void before(T t);
    }

    public interface After<T extends MqSyncMessage> {
        void after(T t, Exception error);
    }

    public static class UnknownEvent extends MqSyncEvent {

        public UnknownEvent(ApplicationContext applicationContext, MqSyncMessage msgObject) {
            super(applicationContext, msgObject);
        }

        @Override
        public void doAction(MqSyncMessage syncMessage) throws Exception {
            log.warn("unknown message [ {} ]", syncMessage.getOriginMessage());
        }
    }

    public static class ParseErrorEvent extends MqSyncEvent {

        private Throwable throwable;

        public ParseErrorEvent(ApplicationContext applicationContext, MqSyncMessage msgObject, Throwable throwable) {
            super(applicationContext, msgObject);
            this.throwable = throwable;
        }

        @Override
        public void doAction(MqSyncMessage syncMessage) throws Exception {
            log.warn("parse error message [ {} ]", syncMessage.getOriginMessage());
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }
}
