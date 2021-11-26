package org.learn.binlogconnector.db;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.learn.binlogconnector.bean.DatabaseConfig;
import org.learn.binlogconnector.mapper.MysqlSchemaMapper;
import org.learn.binlogconnector.mapper.MysqlStatusMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * <pre>
 *
 * </pre>
 *
 */
@Slf4j
public enum DataSourceEnum {
    HDS("hds_config", "hds_config"),

    ;
    @Getter
    private final String database;
    @Getter
    private final String apolloGroupName;

    @Getter
    private TransactionTemplate transactionTemplate;


    private static final ThreadLocal<DataSourceEnum> DATA_SOURCE_ENUM_THREAD_LOCAL = new ThreadLocal<>();

    public DataSource getDataSource() {
        DATA_SOURCE_ENUM_THREAD_LOCAL.set(this);
        return dataSource;
    }


    public MysqlSchemaMapper getMysqlSchemaMapper() {
        DATA_SOURCE_ENUM_THREAD_LOCAL.set(this);
        return null;//getSpringBean(mysqlSchemaMapperClz, MysqlSchemaMapper.class);
    }

    public MysqlStatusMapper getMysqlStatusMapper() {
        DATA_SOURCE_ENUM_THREAD_LOCAL.set(this);
        return null;//getSpringBean(mysqlStatusMapperClz, MysqlStatusMapper.class);
    }

    private <T extends S, S> T getSpringBean(Class<T> clz, Class<S> sup) {
        if (clz == null) {
            throw new RuntimeException(String.format("没有配置对应Mapper的实现：%s,%s", this.name(), sup.getName()));
        }
        return null;//SpringApplicationContext.getBean(clz);
    }

    public static final DataSourceEnum getCurrentThreadDataSourceEnum() {
        return DATA_SOURCE_ENUM_THREAD_LOCAL.get();
    }


    @Getter
    private DatabaseConfig databaseConfig;



    DataSourceEnum(String database, String apolloGroupName) {
        this.database = database;
        this.apolloGroupName = apolloGroupName;
    }

    private static Map<String, String> dbNameMap = new HashMap<>();

    static {
        for (DataSourceEnum data : values()) {
            dbNameMap.put(data.getDatabase(), data.name());
        }
    }

    public static String getDBName(String database) {
        return dbNameMap.get(database);
    }

    public static DataSourceEnum findByName(String name) {
        return Stream.of(values()).filter(item -> item.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public DataSource dataSource;

    public DataSourceEnum init(DataSource dataSource, DatabaseConfig databaseConfig, DataSourceTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.databaseConfig = databaseConfig;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        return this;
    }

    public String uniqueDBServer() {
        if (databaseConfig == null) {
            log.error("数据源连续信息不存在，可能没有配置数据源或存在配置错误：{}", this);
            return null;
        }
        return databaseConfig.getHostname() + "-" + databaseConfig.getPort();
    }

}
