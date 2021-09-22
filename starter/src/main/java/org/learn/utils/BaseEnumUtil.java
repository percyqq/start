package org.learn.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 枚举工具类，只能适用于实现了BaseEnum接口的枚举类
 *
 * @author weidc
 */
public final class BaseEnumUtil {
    private BaseEnumUtil() {
    }

    /**
     * 通过backValue值得到指定类型的枚举类实例
     *
     * @param clazz     实现了BaseEnum接口的枚举类
     * @param backValue 页面隐藏值，数据库中存储值
     * @return
     */
    public final static <E extends Enum<E>> E convertToEnum(Class<E> clazz, Integer backValue) {
        if (clazz == null || backValue == null) {
            return null;
        }
        if (isImplementsBaseEnum(clazz)) {
            E[] enums = clazz.getEnumConstants();
            for (E e : enums) {
                if (((BaseEnum<?>) e).getBackValue() == backValue) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("enum must implements BaseEnum,");
    }

    @SuppressWarnings("unchecked")
    public static List<Integer> getBackValues(BaseEnum<?>[] baseEnums) {
        if (baseEnums == null)
            return Collections.EMPTY_LIST;
        List<Integer> list = new ArrayList<Integer>();
        for (BaseEnum<?> e : baseEnums) {
            if (e != null)
                list.add(e.getBackValue());
        }
        return list;
    }

    public final static <E extends Enum<E>> String enumsToJsonArray(Class<E> clazz) {
        if (clazz == null) {
            return "[]";
        }
        if (isImplementsBaseEnum(clazz)) {
            E[] enums = clazz.getEnumConstants();
            StringBuilder sb = new StringBuilder();
            BaseEnum<?> be;
            for (E e : enums) {
                be = (BaseEnum<?>) e;
                sb.append(sb.length() == 0 ? "" : ",").append(
                        "{\\\"backValue\\\":\\\"" + be.getBackValue() + "\\\",\\\"viewValue\\\":\\\""
                                + be.getViewValue() + "\\\" }");
            }
            return sb.insert(0, "[").append("]").toString();
        }
        throw new IllegalArgumentException("enum must implements BaseEnum");
    }

    public final static <E extends Enum<E>> String enumsToJsonObject(Class<E> clazz) {
        if (clazz == null) {
            return "[]";
        }
        if (isImplementsBaseEnum(clazz)) {
            E[] enums = clazz.getEnumConstants();
            StringBuilder sb = new StringBuilder();
            BaseEnum<?> be;
            for (E e : enums) {
                be = (BaseEnum<?>) e;
                sb.append(sb.length() == 0 ? "" : ",").append(
                        "\\\"" + e.name() + "\\\":{\\\"backValue\\\":\\\"" + be.getBackValue()
                                + "\\\",\\\"viewValue\\\":\\\"" + be.getViewValue() + "\\\"}");
            }
            return sb.insert(0, "{").append("}").toString();
        }
        throw new IllegalArgumentException("enum must implements BaseEnum");
    }

    /**
     * 判断给定clazz是否实现了BaseEnum接口
     *
     * @param clazz
     * @return
     */
    public static <E extends Enum<E>> boolean isImplementsBaseEnum(Class<E> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> c : interfaces) {
            if (c.toString().equals(BaseEnum.class.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据给定的枚举获取数据库值（当Mapper的参数列表为Map时，不能直接用枚举）
     *
     * @param e
     * @return
     */
    public final static <E extends Enum<E> & BaseEnum<E>> Integer getBackValue(E e) {
        return e == null ? null : e.getBackValue();
    }

}
