package org.learn.config.db2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 路由方法拦截器
 *
 * @version V1.0
 * @date 2018/7/11 下午11:00
 */
@Slf4j
public class RoutingMethodInterceptor implements MethodBeforeAdvice, AfterReturningAdvice, ThrowsAdvice {

    public RoutingMethodInterceptor(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    private DataSourceProperties dataSourceProperties;

    private static final ConcurrentHashMap<String, AtomicInteger> COUNTS = new ConcurrentHashMap();

    private List<String> DATASOURCE_NAMES = Collections.EMPTY_LIST;

    private boolean init = false;


    @Override
    public void before(Method method, Object[] objects, Object o) throws Throwable {
        if (!(init || CollectionUtils.isEmpty(dataSourceProperties.getMulti().getSlave()))) {
            DATASOURCE_NAMES = dataSourceProperties.getMulti().getSlave().stream()
                    .map(HikariDataSourceProperties::getName)
                    .collect(Collectors.toList());
        }

        DataSourceRoutable routable = AnnotationUtils.getAnnotation(method, DataSourceRoutable.class);
        if (routable != null) {
            String dataSourceName = routable.value();
            switch (dataSourceProperties.getStrategy()) {
                case SLAVE_RANDOM:
                    String name = routable.value();
                    AtomicInteger count = COUNTS.containsKey(name) ? COUNTS.get(name) : new AtomicInteger(0);
                    COUNTS.putIfAbsent(name, count);
                    count.compareAndSet(DATASOURCE_NAMES.size(), 0);
                    dataSourceName = DATASOURCE_NAMES.get(Math.abs(count.getAndIncrement()) % DATASOURCE_NAMES.size());
                    break;
                case SLAVE_ROUND_ROBIN:
                    dataSourceName = DATASOURCE_NAMES.get(ThreadLocalRandom.current().nextInt(DATASOURCE_NAMES.size()));
                    break;
                default:
                    dataSourceName = routable.value();
            }
            log.info("设置数据源名称: {}", dataSourceName);
            DataSourceNameHolder.set(dataSourceName);
        }
    }

    @Override
    public void afterReturning(Object o, Method method, Object[] objects, Object o1) throws Throwable {
        log.trace("清理数据源名称:{}", DataSourceNameHolder.get());
        DataSourceNameHolder.clear();
    }

    public void afterThrowing(Method method, Object[] args, Object target, Exception ex) {
        // 拦截异常并清理数据源名称，防止发生异常后读取的是之前路由的数据源
        log.trace("异常清理数据源名称:{}", DataSourceNameHolder.get());
        DataSourceNameHolder.clear();
    }

}
