package com.example.client.loadbalancer.ribbon;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * WeightedUptimeRule
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/19
 */
public class WeightedUptimeRule extends RoundRobinRule {

    @Override
    public Server choose(ILoadBalancer lb, Object key) {
        List<Server> serverList = lb.getAllServers();
        if (serverList.isEmpty()) {
            return null;
        }
        int length = serverList.size();
        int[] weights = new int[length];
        int totalWeight = 0;
        for (int i = 0; i < length; i++) {
            Server server = serverList.get(i);
            int weight = getWeight(server);
            totalWeight += weight;
            weights[i] = totalWeight;
        }

        Server server = null;
        if (totalWeight > 0) {
            int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);
            for (int index = 0; index < length; index++) {
                if (weights[index] >= randomWeight) {
                    server = serverList.get(index);
                    break;
                }
            }
        } else {
            server = serverList.get(ThreadLocalRandom.current().nextInt(length));
        }

        if (server != null && server.isAlive()) {
            return server;
        }
        return null;
    }

    int calculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = uptime / (warmup / weight);
        return ww < 1 ? 1 : (Math.min(ww, weight));
    }

    int getWeight(Server server) {
        DiscoveryEnabledServer discoveryEnabledServer = (DiscoveryEnabledServer) server;
        InstanceInfo instanceInfo = discoveryEnabledServer.getInstanceInfo();
        long serviceUpTimestamp = instanceInfo.getLeaseInfo().getServiceUpTimestamp();
        Map<String, String> metadata = instanceInfo.getMetadata();
        int weight = Integer.parseInt(metadata.getOrDefault("weight", "100"));
        if (weight > 0) {
            long uptime = (System.currentTimeMillis() - serviceUpTimestamp);
            if (uptime < 0) {
                return 1;
            }
            int warmup = Integer.parseInt(metadata.getOrDefault("warmup", "600000"));
            if (uptime > 0 && uptime < warmup) {
                weight = calculateWarmupWeight((int) uptime, warmup, weight);
            }
        }
        return Math.max(weight, 0);
    }
}
