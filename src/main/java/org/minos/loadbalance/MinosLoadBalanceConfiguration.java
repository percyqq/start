package org.minos.loadbalance;

import com.netflix.client.config.IClientConfig;
import org.minos.core.context.ContextHolder;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinosLoadbalanceConfiguration
 *
 * @date 2019/9/17
 */
@Configuration
@RibbonClients(defaultConfiguration = MinosRibbonConfiguration.class)
public class MinosLoadBalanceConfiguration {

    @Bean
    public MinosServerChooser minosServerChooser(ContextHolder contextHolder) {
        return new MinosServerChooser(contextHolder);
    }
}
