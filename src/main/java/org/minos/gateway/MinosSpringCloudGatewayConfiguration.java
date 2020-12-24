package org.minos.gateway;

import org.minos.core.context.ContextHolder;
import org.minos.core.context.ThreadLocalContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinosConfiguration
 *
 * @date 2020/06/03
 */
@Configuration
public class MinosSpringCloudGatewayConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ContextHolder contextHolder() {
        return new ThreadLocalContextHolder();
    }

//    @Bean
//    public GrayLoadBalanceFilter grayLoadBalanceFilter(ContextHolder contextHolder, LoadBalancerClient loadBalancer) {
//        return new GrayLoadBalanceFilter(contextHolder, loadBalancer);
//    }
}
