package org.minos.loadbalance;

import com.netflix.client.config.IClientConfig;
import org.springframework.context.annotation.Bean;

/**
 * MinosRibbonConfiguration
 *
 * @date 2019/9/19
 */
public class MinosRibbonConfiguration {

    @Bean
    public MinosLoadBalanceRule minosLoadBalanceRule(IClientConfig clientConfig, ServerChooser serverChooser) {
        MinosLoadBalanceRule rule = new MinosLoadBalanceRule();
        rule.init(clientConfig, serverChooser);
        return rule;
    }
}
