package com.example.client.loadbalancer.ribbon;

import com.example.client.loadbalancer.ribbon.eureka.EurekaDiscoveryEventServerListUpdater;
import com.netflix.client.config.IClientConfig;
import com.netflix.discovery.EurekaClient;
import com.netflix.loadbalancer.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UserServiceRibbonConfiguration
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/19
 */
@Configuration(proxyBeanMethods = false)
public class UserServiceRibbonConfiguration {

    @Bean
    @ConditionalOnBean(EurekaClient.class)
    @ConditionalOnMissingBean
    public ServerListUpdater eurekaDiscoveryEventServerListUpdater(EurekaClient eurekaClient) {
        return new EurekaDiscoveryEventServerListUpdater(eurekaClient);
    }
}
