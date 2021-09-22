package org.minos.core.context;

import java.util.Map;

/**
 * 上下文信息的携带者
 *
 * @date 2019/9/16
 */
public interface ContextHolder {

    /**
     * 获取上下文的内容
     *
     * @param key
     * @return
     */
    Object get(String key);

    /**
     * 获取所有上下文的内容
     */
    Map<String, Object> all();

    /**
     * 设置上下文的内容
     *
     * @param key
     * @param value
     */
    void put(String key, Object value);

    /**
     * 释放上下文内容
     */
    void clear();

    /**
     * 是否存在上下文
     *
     * @param key
     * @return
     */
    boolean containsKey(String key);
}
