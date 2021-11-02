package com;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;


/***
 */
@Slf4j
@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class,}
)

public class ClientApplication implements CommandLineRunner, EnvironmentAware {

    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @EnableDubbo(scanBasePackages = {"com.sth.dubbo.service"}) // @DubboService 实现类 位置
    //@PropertySource("classpath:/dubbo/dubbo-consumer.properties")
    static class ProviderConfiguration {

        @Value("${spring.application.name}")
        private String appName;

        @Bean
        public ApplicationConfig applicationConfig() {
            ApplicationConfig applicationConfig = new ApplicationConfig();
            applicationConfig.setName(appName);
            return applicationConfig;
        }

        @Bean
        public RegistryConfig registryConfig() {
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setId("registry");
            registryConfig.setAddress("zookeeper://127.0.0.1:2181");
            registryConfig.setProtocol("zookeeper");
            return registryConfig;
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        log.info("==============================================");
        log.info("----\t当服务名:{}", this.environment.getProperty("server.context-path"));
        log.info("----\t当服务端口:{}", this.environment.getProperty("server.port"));
        log.info("----\t当应用为:{}", this.environment.getProperty("spring.application.name"));
        log.info("----\t当前环境为:{}", this.environment.getProperty("spring.profiles.active"));
        log.info("----\t注册ZK:{}", this.environment.getProperty("spring.cloud.zookeeper.enabled"));
        log.info("----\t注册ZK:{}", this.environment.getProperty("uniqueid.zookeeper"));
        log.info("==============================================");
    }
}
