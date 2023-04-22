package com.example.client.micrometer.binder.openfeign;

import feign.MethodMetadata;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;

public class OpenFeignMeterRegistryCustomizer implements RequestInterceptor, MeterBinder {

    private MeterRegistry meterRegistry;

    private Counter totalCallsCounter;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        MethodMetadata methodMetadata = requestTemplate.methodMetadata();
        // 方法统计
        Counter counter = Counter.builder("feign.calls")
                .tag("method", methodMetadata.configKey())
                .register(meterRegistry);
        counter.increment();
        // 全局统计
        totalCallsCounter.increment();
    }

    @Override
    public void bindTo(@NonNull MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.totalCallsCounter = Counter.builder("feign.total-calls")
                .register(meterRegistry);
    }
}
