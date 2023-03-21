package com.example.client;

import com.example.client.api.UserService;
import com.example.client.loadbalancer.ribbon.UserServiceRibbonConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * WebClientApplication
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/19
 */
@EnableFeignClients(clients = UserService.class)
@RibbonClient(value = "user-service", configuration = UserServiceRibbonConfiguration.class)
@SpringBootApplication
public class WebClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebClientApplication.class, args);
    }
}
