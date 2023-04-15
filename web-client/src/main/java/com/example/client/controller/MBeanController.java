package com.example.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Set;

@RestController
public class MBeanController {

    private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    @GetMapping("/metrics/{domain}")
    public Set<ObjectName> getMetrics(@PathVariable String domain) throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName(domain);

        return mBeanServer.queryNames(objectName,objectName);
    }
}
