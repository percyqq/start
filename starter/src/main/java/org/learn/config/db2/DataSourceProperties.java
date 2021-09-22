package org.learn.config.db2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wuqiulin
 * @date 2020/4/30 15:13
 * @description
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties extends HikariDataSourceProperties {

    /**
     * 默认数据源
     */
    private String defaultSource;

    /**
     * 数据源负载策略
     */
    private Strategy strategy = Strategy.NONE;

    /**
     * 多数据源配置
     */
    private MultiDataSourceProperties multi;

    @Getter
    public enum Strategy {
        NONE,
        SLAVE_ROUND_ROBIN,
        SLAVE_RANDOM,
        ;


    }
}
