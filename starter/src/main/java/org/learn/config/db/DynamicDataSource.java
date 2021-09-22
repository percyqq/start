package org.learn.config.db;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * @create: 2020-08-10 10:11
 */
@Configuration
public class DynamicDataSource extends AbstractRoutingDataSource implements InitializingBean {


    @Bean(name = "primaryDataSource")
    @Qualifier("primaryDataSource")
    @Primary
    @ConfigurationProperties(prefix = "primary.spring.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "secondaryDataSource")
    @Qualifier("secondaryDataSource")
    @ConfigurationProperties(prefix = "secondary.spring.datasource")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "primaryJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "secondaryJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate(@Qualifier("secondaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Override
    protected Object determineCurrentLookupKey() {
        return JdbcContextHolder.getJdbcType();
    }


    @Override
    public void afterPropertiesSet() {
        System.out.println("init DynamicDataSource dbs ...");

    }


//    @Override
//    public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
//        return null;
//    }
//    @Override
//    public ConnectionBuilder createConnectionBuilder() throws SQLException {
//        return null;
//    }
}
