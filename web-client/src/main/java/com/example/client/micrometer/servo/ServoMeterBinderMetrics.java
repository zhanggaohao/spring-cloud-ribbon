package com.example.client.micrometer.servo;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.convert.ConversionService;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.ToDoubleFunction;

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


        try {
            ObjectName objectName = new ObjectName(SERVO_METRICS_PATTEN);
            Set<ObjectName> objectNameSet = mBeanServer.queryNames(objectName, objectName);
            objectNameSet.forEach(this::buildServoMeter);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildServoMeter(ObjectName objectName) {
        try {
            String name = objectName.getKeyProperty("name");
            String id = objectName.getKeyProperty("id");
            String className = objectName.getKeyProperty("class");
            String type = objectName.getKeyProperty("type");

            MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(objectName);
            MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
            for (MBeanAttributeInfo attribute : attributes) {
                String attributeName = attribute.getName();

                ToDoubleFunction<MBeanServer> toDoubleFunction = mBeanServer -> {
                    try {
                        Object value = mBeanServer.getAttribute(objectName, attributeName);
                        return conversionService.convert(value, Double.class);
                    } catch (Throwable e) {
                    }
                    return 0;
                };
                String meterName = buildMeterName(type, className, id, name);
                switch (type) {
                    case "COUNTER":
                        FunctionCounter.builder(meterName, mBeanServer, toDoubleFunction).register(registry);
                        break;
                    case "GAUGE":
                        Gauge.builder(meterName, mBeanServer, toDoubleFunction).register(registry);
                        break;
                    default:
                        break;

                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String buildMeterName(String type, String className, String id, String name) {
        StringJoiner stringJoiner = new StringJoiner(".");
        stringJoiner.add(type);
        stringJoiner.add(className);
        stringJoiner.add(id);
        stringJoiner.add(name);
        return stringJoiner.toString();
    }

    private void initConversionService(ApplicationReadyEvent event) {
        this.conversionService = event.getApplicationContext().getEnvironment().getConversionService();
    }
}
