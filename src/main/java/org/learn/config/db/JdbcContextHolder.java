package org.learn.config.db;

public class JdbcContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static void setJdbcType(String jdbcType) {
        contextHolder.set(jdbcType);
    }

    public static String getJdbcType() {
        return contextHolder.get();
    }

    /**
     * 恢复成默认的数据源，即defaultTargetDataSource，执行此方法
     */
    public static void clearJdbcType() {
        contextHolder.remove();
    }
}