package org.learn.message.mq;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    public static final String REQUEST_ID = "Request-Id";
    private static final ThreadLocal<Map<String, Object>> kvHolder = new ThreadLocal();

    public static void setRequestId(String requestId) {
        setSessionAttribute(REQUEST_ID, requestId);
    }

    public static void setSessionAttribute(String key, Object value) {
        Map<String, Object> map = kvHolder.get();
        if (map == null) {
            map = new HashMap();
            kvHolder.set(map);
        }

        map.put(key, value);
    }


    public static void remove() {
        kvHolder.remove();
    }
}