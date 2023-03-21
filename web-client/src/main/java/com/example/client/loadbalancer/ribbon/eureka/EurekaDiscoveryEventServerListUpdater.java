package com.example.client.loadbalancer.ribbon.eureka;

import com.netflix.discovery.DiscoveryEvent;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaEvent;
import com.netflix.discovery.EurekaEventListener;
import com.netflix.loadbalancer.ServerListUpdater;

import java.util.Date;

/**
 * DiscoveryClientEventServerListUpdater
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/19
 */
public class EurekaDiscoveryEventServerListUpdater implements ServerListUpdater, EurekaEventListener {

    private final EurekaClient eurekaClient;
    private UpdateAction updateAction;
    private long timestamp;

    public EurekaDiscoveryEventServerListUpdater(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
        eurekaClient.registerEventListener(this);
    }

    @Override
    public void start(UpdateAction updateAction) {
        this.updateAction = updateAction;
        updateAction.doUpdate();
    }

    @Override
    public void stop() {

    }

    @Override
    public String getLastUpdate() {
        return new Date(timestamp).toString();
    }

    @Override
    public long getDurationSinceLastUpdateMs() {
        return 0;
    }

    @Override
    public int getNumberMissedCycles() {
        return 0;
    }

    @Override
    public int getCoreThreads() {
        return 0;
    }

    @Override
    public void onEvent(EurekaEvent eurekaEvent) {
        if (eurekaEvent instanceof DiscoveryEvent) {
            this.timestamp = ((DiscoveryEvent) eurekaEvent).getTimestamp();
            updateAction.doUpdate();
        }
    }
}
