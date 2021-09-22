package org.minos.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import java.util.HashMap;
import java.util.Map;

public class ThreadLocalContextHolder implements ContextHolder {
    static ThreadLocal<Map<String, Object>> holder = new TransmittableThreadLocal();

    public ThreadLocalContextHolder() {
    }

    public static ThreadLocal<Map<String, Object>> getHolder() {
        return holder;
    }

    public static Map<String, Object> get() {
        Map<String, Object> map = (Map)holder.get();
        if (map == null) {
            map = new HashMap();
            holder.set(map);
        }

        return (Map)map;
    }

    public Object get(String key) {
        return get().get(key);
    }

    public Map<String, Object> all() {
        return get();
    }

    public void put(String key, Object value) {
        get().put(key, value);
    }

    public void clear() {
        holder.remove();
    }

    public boolean containsKey(String key) {
        return get().containsKey(key);
    }
}