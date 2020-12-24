package org.learn.config.db2;

import java.lang.annotation.*;

/**
 * 数据源路由注解
 *
 * @version V1.0
 * @date 2018/7/11 下午10:48
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSourceRoutable {

    /**
     * 数据源名称
     *
     * @return
     */
    String value();
}
