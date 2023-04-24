package com.example.client.micrometer;

import com.example.client.micrometer.binder.openfeign.OpenFeignMeterRegistryCustomizer;
import com.example.client.micrometer.binder.servo.ServoMeterBinderMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration(proxyBeanMethods = false)
public class MicrometerConfiguration implements MeterRegistryCustomizer<MeterRegistry> {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private Registration registration;

    @Bean
    public OpenFeignMeterRegistryCustomizer openFeignMeterRegistryCustomizer() {
        return new OpenFeignMeterRegistryCustomizer();
    }

    @Bean
    public ServoMeterBinderMetrics servoMeterBinderMetrics () {
        return new ServoMeterBinderMetrics();
    }

    @Override
    public void customize(MeterRegistry registry) {
        registry.config().commonTags(Arrays.asList(
                Tag.of("application", registration.getServiceId()),
                Tag.of("instance", registration.getInstanceId()),
                Tag.of("host", registration.getHost())
        ));
    }
}
