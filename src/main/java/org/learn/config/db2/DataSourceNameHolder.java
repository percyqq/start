package org.learn.config.db2;

/**
 * 数据源名称持有者
 *
 * @version V1.0
 * @date 2018/7/11 下午10:55
 */
public class DataSourceNameHolder {

    private static final ThreadLocal<String> LOCAL = new ThreadLocal<String>();

    /**
     * 获取数据源名称
     *
     * @return
     */
    public static String get() {
        return LOCAL.get();
    }

    /**
     * 设置数据源名称
     *
     * @param dataSourceName
     */
    public static void set(String dataSourceName) {
        LOCAL.set(dataSourceName);
    }

    /**
     * 清除数据源名称
     */
    public static void clear() {
        LOCAL.set(null);
    }
}
