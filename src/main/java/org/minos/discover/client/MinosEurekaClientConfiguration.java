package org.minos.discover.client;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.loadbalancer.ServerListUpdater;
import org.minos.discover.client.listener.DiscoveryCacheEventPublisher;
import org.minos.discover.client.loadbalancer.EurekaNotificationServerListUpdater;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.netflix.eureka.CloudEurekaInstanceConfig;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.cloud.netflix.eureka.InstanceInfoFactory;
import org.springframework.cloud.netflix.eureka.config.DiscoveryClientOptionalArgsConfiguration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.CollectionUtils;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(EurekaClientConfig.class)
@Import(DiscoveryClientOptionalArgsConfiguration.class)
@ConditionalOnExpression("${eureka.client.enabled:true} && ${minos.eureka.client.enable:true}")
@AutoConfigureBefore({NoopDiscoveryClientAutoConfiguration.class,
        CommonsClientAutoConfiguration.class,
        ServiceRegistryAutoConfiguration.class,
        EurekaClientAutoConfiguration.class,
})
@AutoConfigureAfter(name = {
        "org.springframework.cloud.autoconfigure.RefreshAutoConfiguration",
        "org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration",
        "org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration"})
public class MinosEurekaClientConfiguration {

    private ConfigurableEnvironment env;


    public MinosEurekaClientConfiguration(ConfigurableEnvironment env) {
        //super(env);
        this.env = env;
    }

    @ConditionalOnMissingBean(value = MinosEurekaClientConfigBean.class, search = SearchStrategy.CURRENT)
    @Bean
    @ConfigurationProperties(MinosEurekaClientConfigBean.PREFIX)
    public MinosEurekaClientConfigBean minosEurekaClientConfigBean() {
        return new MinosEurekaClientConfigBean();
    }

    @Bean
    @ConditionalOnMissingBean
    public DiscoveryCacheEventPublisher cacheEventPublisher() {
        return new DiscoveryCacheEventPublisher();
    }

    @Bean
    @Primary
    public ServerListUpdater eurekaServerListUpdater(DiscoveryCacheEventPublisher cacheEventPublisher) {
        return new EurekaNotificationServerListUpdater(cacheEventPublisher);
    }

    @Bean
    public MinosEurekaBeanPostProcessor minosEurekaBeanPostProcessor() {
        return new MinosEurekaBeanPostProcessor();
    }


    @Configuration
    @ConditionalOnExpression("${eureka.client.enabled:true} && ${minos.eureka.client.enable:true}")
    protected static class EurekaClientConfiguration {

        @Autowired
        private ApplicationContext context;

        @Autowired
        private AbstractDiscoveryClientOptionalArgs<?> optionalArgs;


        @Bean(destroyMethod = "shutdown")
        @ConditionalOnMissingBean(value = EurekaClient.class, search = SearchStrategy.CURRENT)
        public EurekaClient eurekaClient(ApplicationInfoManager manager,
                                         EurekaClientConfigBean config,
                                         MinosEurekaClientConfigBean minosConfig,
                                         DiscoveryCacheEventPublisher cacheEventPublisher) throws IllegalAccessException {
            //两种同步只能开一种
            if (minosConfig.isEnable()) {
                config.setFetchRegistry(false);
            }
            return new MinosEurekaClient(manager, config, minosConfig, this.optionalArgs, this.context, cacheEventPublisher);
        }

        @Bean
        @ConditionalOnMissingBean(value = ApplicationInfoManager.class, search = SearchStrategy.CURRENT)
        public ApplicationInfoManager eurekaApplicationInfoManager(
                EurekaInstanceConfig config, MinosEurekaClientConfigBean minosConfig) {
            InstanceInfo instanceInfo = new InstanceInfoFactory().create(config);
            if (!CollectionUtils.isEmpty(minosConfig.getMetadata())) {
                instanceInfo.getMetadata().putAll(minosConfig.getMetadata());
            }
            return new ApplicationInfoManager(config, instanceInfo);
        }

        @Bean
        @ConditionalOnBean(AutoServiceRegistrationProperties.class)
        @Primary
        @ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
        public EurekaRegistration eurekaMinosRegistration(EurekaClient eurekaClient,
                                                     CloudEurekaInstanceConfig instanceConfig,
                                                     ApplicationInfoManager applicationInfoManager,
                                                     @Autowired(required = false) ObjectProvider<HealthCheckHandler> healthCheckHandler) {
            return EurekaRegistration.builder(instanceConfig).with(applicationInfoManager)
                    .with(eurekaClient).with(healthCheckHandler).build();
        }


    }
}
