package org.learn.config.db2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.util.Map;

/**
 * @date 2020/4/30 14:49
 * @description
 */
@Getter
@Setter
public class HikariDataSourceProperties extends DataSourceProperties {

    /**
     * 用于设置超时时间等
     */
    private Map<String, Object> hikari;


}
