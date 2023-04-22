package com.example.client.micrometer.binder.redis.wrapper;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass;

/**
 * RedisConnectionFactoryWrapperBeanPostProcessor
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/4/20
 */
public class RedisConnectionFactoryWrapperBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = ultimateTargetClass(bean);
        if (RedisConnectionFactory.class.isAssignableFrom(beanClass)) {
            return new RedisConnectionFactoryWrapper((RedisConnectionFactory) bean);
        }
        return bean;
    }
}
