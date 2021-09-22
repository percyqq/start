package org.learn.config.db2;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @date 2020/4/30 14:40
 * @description
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
@ConditionalOnClass(value = {HikariDataSource.class, JdbcTemplate.class})
public class MultiDataSourceAutoConfiguration {

    /**
     * 路由方法Advisor顺序
     */
    public static final int ROUTING_METHOD_ADVISOR_ORDER = -999;

    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(DataSourceProperties properties) {
        Map<Object, Object> targetDataSources = new HashMap<>(8);
        String defaultSource = properties.getDefaultSource();
        MultiDataSourceProperties multi = properties.getMulti();
        Class<? extends DataSource> type = properties.getType();
        String driverClassName = properties.getDriverClassName();
        Map<String, Object> defaultConfig = properties.getHikari();
        if (defaultConfig == null) {
            defaultConfig = new HashMap<>(16);
        }
        HikariDataSourceProperties masterProperties = multi.getMaster();
        if (Objects.isNull(masterProperties)) {
            throw new IllegalArgumentException("必须有master数据源");
        }
        // master节点 默认节点
        DataSource defaultDataSource = createDataSource(defaultConfig, type, driverClassName, masterProperties);
        List<HikariDataSourceProperties> slave = multi.getSlave();
        targetDataSources.put(masterProperties.getName(), defaultDataSource);

        // 遍历其他数据源
        DataSource customerDataSource;
        if (!CollectionUtils.isEmpty(slave)) {
            for (HikariDataSourceProperties property : slave) {
                customerDataSource = createDataSource(defaultConfig, type, driverClassName, property);
                targetDataSources.put(property.getName(), customerDataSource);
                // 默认数据源
                if (Objects.equals(defaultSource, property.getName())) {
                    defaultDataSource = customerDataSource;
                }
            }
        }

        // 初始化代理路由数据源
        RoutingDataSource proxyDataSource = new RoutingDataSource();
        proxyDataSource.setDefaultTargetDataSource(defaultDataSource);
        proxyDataSource.setTargetDataSources(targetDataSources);
        return proxyDataSource;
    }

    private DataSource createDataSource(Map<String, Object> defaultConfig, Class<? extends DataSource> type, String driverClassName, HikariDataSourceProperties property) {
        DataSource customerDataSource;
        HashMap<String, Object> configProperties = new HashMap<>(defaultConfig);
        customerDataSource = crateDataSource(type, driverClassName, property);
        Map<String, Object> config = property.getHikari();
        if (!CollectionUtils.isEmpty(config)) {
            configProperties.putAll(config);
        }
        log.info("init datasource name {} jdbc {}", property.getName(), property.getUrl());
        bind(customerDataSource, configProperties);
        return customerDataSource;
    }

    private DataSource crateDataSource(Class<? extends DataSource> type, String driverClassName, HikariDataSourceProperties property) {
        DataSource customerDataSource;
        DataSourceBuilder<?> builder = property.initializeDataSourceBuilder();
        if (property.getType() == null) {
            builder.type(type);
        } else {
            builder.type(property.getType());
        }
        if (property.getDriverClassName() == null) {
            builder.driverClassName(driverClassName);
        } else {
            builder.driverClassName(property.getDriverClassName());
        }
        customerDataSource = builder.build();
        return customerDataSource;
    }

    /**
     * 绑定扩展参数
     *
     * @param result
     * @param properties
     */
    private void bind(DataSource result, Map properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source);
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(result));
    }

    @Bean
    public RoutingMethodAdvisor routingMethodAdvisor(DataSourceProperties properties) {
        try {
            RoutingMethodAdvisor advisor = new RoutingMethodAdvisor();
            advisor.setAdvice(new RoutingMethodInterceptor(properties));
            advisor.setOrder(ROUTING_METHOD_ADVISOR_ORDER);
            return advisor;
        } finally {
            log.info("数据源路由器初始化成功.");
        }
    }

}
