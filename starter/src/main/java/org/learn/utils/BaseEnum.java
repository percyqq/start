package org.learn.utils;

/**
 * 枚举类需要实现接口
 *
 * @param <E>
 * @author weidc
 */
public interface BaseEnum<E extends Enum<E>> {
    /**
     * backValue 页面隐藏值，数据库中贮存值
     *
     * @return
     */
    Integer getBackValue();

    /**
     * viewValue 页面显示值
     *
     * @return
     */
    String getViewValue();
}
