package org.learn.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * @description:
 * @author: qingqing
 * @create: 2020-08-03 10:25
 */
public class CollectionUtils {


    public static int size(Object object) {
        if (object == null) {
            return 0;
        } else {
            int total = 0;
            if (object instanceof Map) {
                total = ((Map) object).size();
            } else if (object instanceof Collection) {
                total = ((Collection) object).size();
            } else if (object instanceof Object[]) {
                total = ((Object[]) object).length;
            } else if (object instanceof Iterator) {
                Iterator it = (Iterator) object;
                while (it.hasNext()) {
                    ++total;
                    it.next();
                }
            } else if (object instanceof Enumeration) {
                Enumeration it = (Enumeration) object;
                while (it.hasMoreElements()) {
                    ++total;
                    it.nextElement();
                }
            } else if (object.getClass().isArray()) {
                try {
                    total = Array.getLength(object);
                } catch (IllegalArgumentException var3) {
                    throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
                }
            } else {
                total = 1;
            }

            return total;
        }
    }
}
