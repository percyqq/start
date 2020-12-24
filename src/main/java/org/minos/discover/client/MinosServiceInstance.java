package org.minos.discover.client;

/**
 * @date 2020/7/1 10:50
 */
public interface MinosServiceInstance {
    /**
     * @return 服务ID，比如Eureka为服务名
     */
    String serviceId();

    /**
     * @return 实例ID
     */
    String instanceId();
}