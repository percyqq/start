package org.minos.discover.client;

import java.util.HashMap;
import java.util.Map;

/**
 * 扩展EurekaClient配置
 */
public class MinosEurekaClientConfigBean {
    public static final String PREFIX = "minos.eureka.client";

    private boolean enable = true;

    private Map<String, String> metadata = new HashMap<>();

    private LongPolling longPolling = new LongPolling();

    private ForceFetch forceFetch = new ForceFetch();

    public static class LongPolling {
        /**
         * seconds
         */
        private int timeout = 30;

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }

    public static class ForceFetch {
        /**
         * seconds
         */
        private int interval = 60;

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public LongPolling getLongPolling() {
        return longPolling;
    }

    public void setLongPolling(LongPolling longPolling) {
        this.longPolling = longPolling;
    }

    public ForceFetch getForceFetch() {
        return forceFetch;
    }

    public void setForceFetch(ForceFetch forceFetch) {
        this.forceFetch = forceFetch;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
