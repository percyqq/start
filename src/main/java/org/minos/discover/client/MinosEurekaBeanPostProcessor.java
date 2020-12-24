package org.minos.discover.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;

import java.lang.reflect.Field;

/**
 * BPP，用于处理Refresh Context时避免刷新Eureka
 *
 * @date 2020-04-27 14:54
 */
public class MinosEurekaBeanPostProcessor implements BeanPostProcessor {
    private static final Log LOGGER = LogFactory.getLog(MinosEurekaClient.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.endsWith("EurekaClientConfigurationRefresher")) {
            try {
                LOGGER.info("Minos try to remove EurekaClientConfigurationRefresher autoRegistration");

                Field autoRegistration =
                        MinosEurekaDiscoveryClientConfiguration.MinosEurekaClientConfigurationRefresher.class.getSuperclass().getDeclaredField(
                                "autoRegistration");

                autoRegistration.setAccessible(true);
                autoRegistration.set(bean, null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.warn("Minos Remove EurekaClientConfigurationRefresher autoRegistration fail", e);
            }
        }
        return bean;
    }

    private class MinosEurekaDiscoveryClientConfiguration extends EurekaDiscoveryClientConfiguration {
        public class MinosEurekaClientConfigurationRefresher extends EurekaDiscoveryClientConfiguration.EurekaClientConfigurationRefresher {

        }
    }
}
