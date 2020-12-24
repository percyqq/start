package org.learn.config.db2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 路由数据源
 *
 * @version V1.0
 * @date 2018/7/11 下午11:07
 */
@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceName = DataSourceNameHolder.get();
        log.debug("切换数据源:{}", dataSourceName);
        return dataSourceName;
    }
}
