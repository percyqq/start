package org.minos.discover.client.eureka;

import com.netflix.discovery.shared.resolver.EurekaEndpoint;

import java.util.List;

/**
 * 服务列节点选择器
 * @date 2020/7/1 13:14
 */
public interface EndPointSelector<T extends EurekaEndpoint> {
    List<T> endpoints();

    T select();

    T select(boolean reselect);
}