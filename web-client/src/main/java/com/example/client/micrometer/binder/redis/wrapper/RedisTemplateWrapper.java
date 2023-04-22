package com.example.client.micrometer.binder.redis.wrapper;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;

/**
 * RedisTemplateWrapper
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/4/19
 */
public class RedisTemplateWrapper<K, V> extends RedisTemplate<K, V> {

    @Override
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        return newProxyRedisConnection(connection);
    }

    private RedisConnection newProxyRedisConnection(RedisConnection connection) {
        return (RedisConnection) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class[]{RedisConnection.class},
                new RedisConnectionInvocationHandler(connection));
    }
}
