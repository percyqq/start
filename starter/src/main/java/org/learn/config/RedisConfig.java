package org.learn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * @author xin
 * @date 2019-06-18
 */

@Configuration
public class RedisConfig {

    @Autowired
    private RedisProperties redisProperties;

    @Bean("redisTemplate")
    public RedisTemplate redisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    private JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 最大连接数
        jedisPoolConfig.setMaxTotal(redisProperties.getMaxActive());
        // 当池内没有可用连接时，最大等待时间
        jedisPoolConfig.setMaxWaitMillis(redisProperties.getMaxWait());
        // 最大空闲连接数
        jedisPoolConfig.setMinIdle(redisProperties.getMinIdle());
        // 最小空闲连接数
        jedisPoolConfig.setMinIdle(redisProperties.getMinIdle());
        // 其他属性可以自行添加
        return jedisPoolConfig;
    }

    private RedisConnectionFactory redisConnectionFactory() {
        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().usePooling()
                .poolConfig(jedisPoolConfig()).and().readTimeout(Duration.ofMillis(redisProperties.getTimeout())).build();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());
        redisStandaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

//    @Bean("redisTemplate")
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setKeySerializer(RedisSerializer.string());
//        template.setHashKeySerializer(RedisSerializer.string());
//        template.setConnectionFactory(redisConnectionFactory);
//
//        return template;
//    }

}
