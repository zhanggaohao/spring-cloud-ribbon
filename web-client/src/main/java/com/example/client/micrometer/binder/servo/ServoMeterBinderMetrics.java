package com.example.client.micrometer.binder.servo;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.convert.ConversionService;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

public class ServoMeterBinderMetrics implements MeterBinder, ApplicationListener<ApplicationReadyEvent> {

    private final static String SERVO_METRICS_PATTEN = "com.netflix.servo:*";

    private MeterRegistry registry;

    private MBeanServer mBeanServer;

    private ConversionService conversionService;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();

        initConversionService(event);

        registerServoMetrics();
    }

    private void registerServoMetrics() {
        Set<ObjectName> objectNames = findServoMBeanObjectNames();
        for (ObjectName objectName : objectNames) {
            registerServoMeter(objectName);
        }
    }

    private Set<ObjectName> findServoMBeanObjectNames() {
        Set<ObjectName> objectNames = Collections.emptySet();
        try {
            ObjectName objectName = new ObjectName(SERVO_METRICS_PATTEN);
            objectNames = mBeanServer.queryNames(objectName, objectName);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
        return objectNames;
    }

    private void registerServoMeter(ObjectName objectName) {
        try {
            MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(objectName);

            String type = objectName.getKeyProperty("type");

            MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
            for (MBeanAttributeInfo attribute : attributes) {
                String attributeName = attribute.getName();
                String meterName = buildMeterName(objectName, attributeName);

                ToDoubleFunction<MBeanServer> toDoubleFunction = mBeanServer -> {
                    Double value = null;
                    try {
                        Object attributeValue = mBeanServer.getAttribute(objectName, attributeName);
                        value = conversionService.convert(attributeValue, Double.class);
                    } catch (Throwable ignored) {
                    }
                    return value == null ? 0 : value;
                };

                switch (type) {
                    case "COUNTER":
                        FunctionCounter.builder(meterName, mBeanServer, toDoubleFunction).register(registry);
                        break;
                    case "GAUGE":
                        Gauge.builder(meterName, mBeanServer, toDoubleFunction).register(registry);
                        break;
                    case "NORMALIZED":
                        ToLongFunction<MBeanServer> toLongFunction = mBeanServer -> {
                            Long value = null;
                            try {
                                Object attributeValue = mBeanServer.getAttribute(objectName, attributeName);
                                value = conversionService.convert(attributeValue, Long.class);
                            } catch (Throwable ignored) {
                            }
                            return value == null ? 0 : value;
                        };
                        FunctionTimer.builder(meterName, mBeanServer, toLongFunction, toDoubleFunction, TimeUnit.MILLISECONDS)
                                .register(registry);
                        break;
                    default:
                        break;

                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String buildMeterName(ObjectName objectName, String attributeName) {
        StringJoiner stringJoiner = new StringJoiner(".");

        String name = objectName.getKeyProperty("name");
        String id = objectName.getKeyProperty("id");
        String className = objectName.getKeyProperty("class");
        String type = objectName.getKeyProperty("type");

        append(stringJoiner, type)
                .append(stringJoiner, className)
                .append(stringJoiner, id)
                .append(stringJoiner, name)
                .append(stringJoiner, attributeName);

        return stringJoiner.toString();
    }

    private ServoMeterBinderMetrics append(StringJoiner stringJoiner, String str) {
        stringJoiner.add(str);
        return this;
    }

    private void initConversionService(ApplicationReadyEvent event) {
        this.conversionService = event.getApplicationContext().getEnvironment().getConversionService();
    }
}
