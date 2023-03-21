# 基于Ribbon的负载均衡
基于Ribbon的负载均衡，注册中心是Eureka，客户端负载均衡通过uptime（上线时间）的warmup（预热时间）计算权重。


## 服务端上报
服务端启动后上报服务时间、预热时间、和默认权重。
```java
@Configuration(proxyBeanMethods = false)
public class MetadataUploadConfiguration implements ApplicationRunner {

    private final ApplicationInfoManager applicationInfoManager;
    
    public MetadataUploadConfiguration(EurekaClient eurekaClient) {
        this.applicationInfoManager = eurekaClient.getApplicationInfoManager();
    }
    
    @Override
    public void run(ApplicationArguments args) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
        metadata.put("warmup", String.valueOf(10 * 60 * 1000));
        metadata.put("weight", "80");
        applicationInfoManager.registerAppMetadata(metadata);
    }
}
```


## 客户端计算权重
客户端获取服务端集群后，对服务节点进行权重计算。
```java
public class WeightedUptimeRule extends RoundRobinRule {
    
    int calculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = uptime / (warmup / weight);
        return ww < 1 ? 1 : (Math.min(ww, weight));
    }

    int getWeight(Server server) {
        DiscoveryEnabledServer discoveryEnabledServer = (DiscoveryEnabledServer) server;
        InstanceInfo instanceInfo = discoveryEnabledServer.getInstanceInfo();
        Map<String, String> metadata = instanceInfo.getMetadata();
        int weight = Integer.parseInt(metadata.getOrDefault("weight", "100"));
        if (weight > 0) {
            long timestamp = Long.parseLong(metadata.getOrDefault("timestamp", "0L"));
            if (timestamp > 0) {
                long uptime = (System.currentTimeMillis() - timestamp);
                if (uptime < 0) {
                    return 1;
                }
                int warmup = Integer.parseInt(metadata.getOrDefault("warmup", "600000"));
                if (uptime > 0 && uptime < warmup) {
                    weight = calculateWarmupWeight((int) uptime, warmup, weight);
                }
            }
        }
        return Math.max(weight, 0);
    }
}
```