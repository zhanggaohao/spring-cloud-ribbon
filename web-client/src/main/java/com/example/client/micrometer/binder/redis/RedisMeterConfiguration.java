package com.example.client.micrometer.binder.redis;

import com.example.client.micrometer.binder.redis.wrapper.RedisConnectionFactoryWrapperBeanPostProcessor;
import com.example.client.micrometer.binder.redis.wrapper.RedisTemplateWrapper;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisMeterConfiguration {

    @Bean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplateWrapper<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * TODO Spring Boot Actuator Starter依赖了Spring Boot Actuator AutoConfigure，
     * RedisConnectionFactoryWrapped 没有对 ReactiveRedisConnectionFactory 进行 Wrapper
     * 系统启动时 RedisReactiveHealthContributorAutoConfiguration，无法获取 ReactiveRedisConnectionFactory 会导致启动报错
     */
//    @Bean
//    public BeanPostProcessor redisConnectionFactoryWrapperBeanPostProcessor() {
//        return new RedisConnectionFactoryWrapperBeanPostProcessor();
//    }
}
