package org.learn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedisProperties {
    private int database = 0;
    private String url;
    private String host = "localhost";
    private String password;
    private int port = 6379;
    private int timeout;
    private int maxIdle = 8;
    private int minIdle = 0;
    private int maxActive = 8;
    private int maxWait = -1;
    private boolean testOnBorrow = false;

}