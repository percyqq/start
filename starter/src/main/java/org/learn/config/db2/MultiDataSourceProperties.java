package org.learn.config.db2;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @date 2020/4/30 16:42
 * @description
 */
@Setter
@Getter
public class MultiDataSourceProperties {

    private HikariDataSourceProperties master;

    private List<HikariDataSourceProperties> slave;
}
