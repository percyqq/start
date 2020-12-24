package org.minos.loadbalance;

import com.netflix.loadbalancer.Server;

import java.util.List;

/**
 * 服务实例选择器
 *
 * @date 2019/9/18
 */
public interface ServerChooser {

    /**
     * 根据给定的服务示例列表，选择一个示例
     *
     * @param serverList
     * @return
     */
    Server choose(List<Server> serverList);
}
