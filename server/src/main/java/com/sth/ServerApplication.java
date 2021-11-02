package com.sth;


import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.EnvironmentAware;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;


/***
 *  dubbo 2.7.x 系列
 *  使用jdk8，
 *  zk使用3.4.x
 *
 */
@Slf4j
@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class,}
)

@EnableAsync
//@ImportResource({"classpath:dubbo/*.xml"})
public class ServerApplication implements CommandLineRunner, EnvironmentAware {

    private Environment environment;

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    //@Configuration
    @EnableDubbo(scanBasePackages = {"com.sth.dubbo"}) // @DubboService 实现类 位置
    //@PropertySource("classpath:/dubbo/dubbo-provider.properties")
    static class ProviderConfiguration {
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
