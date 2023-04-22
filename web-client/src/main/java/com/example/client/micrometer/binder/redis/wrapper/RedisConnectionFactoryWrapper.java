package com.example.client.micrometer.binder.redis.wrapper;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;

/**
 * RedisConnectFactoryWrapper
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/4/20
 */
public class RedisConnectionFactoryWrapper implements RedisConnectionFactory {

    private final RedisConnectionFactory delegate;

    public RedisConnectionFactoryWrapper(RedisConnectionFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public RedisConnection getConnection() {
        RedisConnection connection = delegate.getConnection();
        return newProxyRedisConnection(connection);
    }

    private RedisConnection newProxyRedisConnection(RedisConnection connection) {
        return (RedisConnection) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class[]{RedisConnection.class},
                new RedisConnectionInvocationHandler(connection));
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return delegate.getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return delegate.getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return delegate.getSentinelConnection();
    }

    @Override
    @Nullable
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return delegate.translateExceptionIfPossible(ex);
    }
}
