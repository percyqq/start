package org.minos.discover.client.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.transport.EurekaHttpClient;
import org.minos.discover.shared.DiscoveryInstance;
import org.minos.discover.shared.DiscoveryMonitorInfo;

import java.util.List;

/**
 * @date 2020/8/7 08:45
 */
public interface MinosEurekaHttpClient extends EurekaHttpClient {
    /**
     * long-polling接口
     */
    ServerLongPollingResponse epoch(long epoch, boolean delta);

    /**
     * 获取所有应用实例信息（没有缓存）
     */
    List<Application> getApplications();

    /**
     * long-polling monitor
     */
    DiscoveryMonitorInfo monitor();

    /**
     * 根据IP下线实例接口
     * 注意：
     * 1. 如果是K8S服务，由于POD IP是变化的，所以不需要管
     * 2. 如果是ECS服务，设置状态后，需要在此设置将服务状态恢复
     */
    DiscoveryInstance setStatus(String ip, InstanceInfo.InstanceStatus status);
}
