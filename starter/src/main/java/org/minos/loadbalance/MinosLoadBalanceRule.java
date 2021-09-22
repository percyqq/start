package org.minos.loadbalance;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MinosLoadBalanceRule
 *
 * @date 2019/9/17
 */
public class MinosLoadBalanceRule extends AbstractLoadBalancerRule {

    private ServerChooser serverChooser;

    private IClientConfig clientConfig;

    private static Logger logger = LoggerFactory.getLogger("minos-loadbalance");

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        this.clientConfig = iClientConfig;
    }

    public void init(IClientConfig clientConfig, ServerChooser serverChooser) {
        logger.debug("MinosLoadBalanceRule init with ServerChooser:{}", serverChooser);

        this.serverChooser = serverChooser;
        this.initWithNiwsConfig(clientConfig);
    }

    @Override
    public Server choose(Object o) {
        logger.debug("MinosLoadBalanceRule start choose a server");

        List<Server> serverList = getLoadBalancer().getAllServers();
        return serverChooser.choose(serverList);
    }
}
