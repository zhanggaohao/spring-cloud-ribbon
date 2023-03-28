package com.example.client.fault.tolerance.web.config;

import com.example.client.fault.tolerance.web.ResourceBulkheadInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * FaultToleranceConfiguration
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/28
 */
@Import(ResourceBulkheadInterceptor.class)
@Configuration(proxyBeanMethods = false)
public class FaultToleranceConfiguration implements WebMvcConfigurer {

    private final ResourceBulkheadInterceptor resourceBulkheadInterceptor;

    public FaultToleranceConfiguration(ResourceBulkheadInterceptor resourceBulkheadInterceptor) {
        this.resourceBulkheadInterceptor = resourceBulkheadInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(resourceBulkheadInterceptor);
    }
}
