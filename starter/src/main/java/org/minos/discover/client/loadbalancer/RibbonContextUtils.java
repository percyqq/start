package org.minos.discover.client.loadbalancer;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @date 2020-09-01
 */
public class RibbonContextUtils {
    public static Map<String, List<Server>> ribbonClientInfo(SpringClientFactory ribbonContexts) {
        return ribbonContexts.getContextNames().stream().collect(Collectors
                .toMap(Function.identity(), name -> ribbonContexts.getLoadBalancer(name).getAllServers()));
    }
}
