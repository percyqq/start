package org;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableAsync

@SpringBootApplication(
        scanBasePackages = "org.learn",
        exclude = { DataSourceAutoConfiguration.class, RedisAutoConfiguration.class, //JedisConnectionConfiguration.class
})
//@ComponentScan(basePackages = {"org.learn", "com"},
//        excludeFilters = {@ComponentScan.Filter(type = FilterType.CUSTOM, classes = ExcludeOuterConfigFilter.class)}
//)

@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@MapperScan({"org.learn.**.dao", "org.learn.**.mapper"})
@ServletComponentScan(basePackages = "org.learn")
@EnableFeignClients( basePackages = {"org.learn"})

@EnableRetry(proxyTargetClass = true)
public class StartApplication implements CommandLineRunner, EnvironmentAware, ApplicationContextAware {

    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
    }


    //org.redisson.Redisson redisson;
    DefaultSingletonBeanRegistry d;

    org.aspectj.lang.annotation.Pointcut f;

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
