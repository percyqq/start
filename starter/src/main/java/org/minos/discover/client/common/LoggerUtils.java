package org.minos.discover.client.common;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

public class LoggerUtils {

    public static final Logger CLIENT_LOGGER;
    public static final Logger HTTP_LOGGER;
    public static final Logger WATCHER_LOGGER;
    public static final Logger UPDATER_LOGGER;

    static {
        HTTP_LOGGER = getLogger("com.xx.infra.minos.discovery.client.http");
        CLIENT_LOGGER = getLogger("com.xx.infra.minos.discovery.client");
        WATCHER_LOGGER = getLogger("com.xx.infra.minos.discovery.client.eureka");
        UPDATER_LOGGER = getLogger("com.xx.infra.minos.discovery.updater");
    }

    public static Logger logger(Class<?> clazz) {
        return getLogger(clazz);
    }

}
