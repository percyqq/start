package org.learn.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @create: 2020-12-25 17:01
 */
@Slf4j
@Component
public class RedisionTemplate {

    @Resource
    private RedissonClient redissonClient;

    public interface RedissonLockExecutor<T> {
        /**
         * 执行业务逻辑
         *
         * @return
         */
        T execute();

        /**
         * 锁名称
         *
         * @return
         */
        String getLockName();
    }

    public <T> T execute(RedissonLockExecutor<T> executor) {
        RLock lock = redissonClient.getLock(executor.getLockName());
        if (lock.isLocked()) {
            log.info("repeat request execute {}", executor.getLockName());
            throw new RuntimeException("msg");
        }
        lock.lock();
        try {
            return executor.execute();
        } finally {
            if (lock != null && lock.isLocked()) {
                lock.unlock();
            }

        }

    }


}
