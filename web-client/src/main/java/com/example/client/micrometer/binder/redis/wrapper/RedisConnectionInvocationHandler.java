package com.example.client.micrometer.binder.redis.wrapper;

import org.apache.commons.lang3.ClassUtils;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

class RedisConnectionInvocationHandler implements InvocationHandler {

    private final RedisConnection redisConnection;

    private static List<Class<?>> invocationRedisCommands;

    RedisConnectionInvocationHandler(RedisConnection redisConnection) {
        this.redisConnection = redisConnection;
    }

    static {
        initInvocationRedisCommandClasses();
    }

    private static void initInvocationRedisCommandClasses() {
        invocationRedisCommands = Collections.unmodifiableList(ClassUtils.getAllInterfaces(RedisCommands.class));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object returnValue = null;
        Throwable t = null;
        Class<?> declaringClass = method.getDeclaringClass();
        if (!invocationRedisCommands.contains(declaringClass)) {
            returnValue = method.invoke(redisConnection, args);
        } else {
            beforeExecute();
            try {
                returnValue = method.invoke(redisConnection, args);
            } catch (Throwable throwable) {
                throw t = throwable;
            } finally {
                afterExecute(returnValue, t);
            }
        }
        return returnValue;
    }

    private void beforeExecute() {
        System.out.println("执行前");
    }

    private void afterExecute(Object returnValue, Throwable throwable) {
        if (throwable != null) {
            System.out.println("执行发生异常: " + throwable.getMessage());
        }
        System.out.println("执行完毕");
    }
}