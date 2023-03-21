package com.example.client.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * MetadataUploadConfiguration
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/20
 */
@Configuration(proxyBeanMethods = false)
public class MetadataUploadConfiguration implements ApplicationRunner {

    private final EurekaClient eurekaClient;
    private final ApplicationInfoManager applicationInfoManager;
    private final InstanceInfo instanceInfo;

    public MetadataUploadConfiguration(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
        this.applicationInfoManager = eurekaClient.getApplicationInfoManager();
        this.instanceInfo = applicationInfoManager.getInfo();
    }

//    @Scheduled(fixedRate = 5 * 1000, initialDelay = 1000)
//    public void uploadUptime() {
//        Map<String, String> metadata = new HashMap<>();
//        metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
//        eurekaClient.getApplicationInfoManager().registerAppMetadata(metadata);
//    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
        metadata.put("warmup", String.valueOf(10 * 60 * 1000));
        metadata.put("weight", "80");
        applicationInfoManager.registerAppMetadata(metadata);
    }
}
