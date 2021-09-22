package org.learn.config.db;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class DataSourceAspect {

    @Around("@annotation(dataSource0)")
    public Object execute(ProceedingJoinPoint joinPoint, DataSourceType dataSource0) throws Throwable {
        try {
            JdbcContextHolder.setJdbcType(dataSource0.value());
            return joinPoint.proceed();
        } finally {
            JdbcContextHolder.clearJdbcType();
        }
    }
}