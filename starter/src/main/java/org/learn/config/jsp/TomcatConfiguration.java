package org.learn.config.jsp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @date: 2020/03/24
 * @description:
 */
@Configuration
@ConditionalOnProperty(name = "tomcat.staticResourceCustomizer.enabled", matchIfMissing = true)
public class TomcatConfiguration {
    @Bean
    public WebServerFactoryCustomizer<WebServerFactory> staticResourceCustomizer() {
        return factory -> {
            if (factory instanceof TomcatServletWebServerFactory) {
                ((TomcatServletWebServerFactory) factory)
                        .addContextCustomizers(context -> context.addLifecycleListener(new StaticResourceConfigurer(context)));
            }
        };
    }
}
