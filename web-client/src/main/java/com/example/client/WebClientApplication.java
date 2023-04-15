package com.example.client;

import com.example.client.api.UserService;
import com.example.client.loadbalancer.ribbon.UserServiceRibbonConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * WebClientApplication
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/19
 */
@ServletComponentScan(basePackages = "com.example.client")
@EnableFeignClients(clients = UserService.class)
@RibbonClient(value = "user-service", configuration = UserServiceRibbonConfiguration.class)
@SpringBootApplication
@EnableScheduling
public class WebClientApplication implements BeanFactoryAware {

    private BeanFactory beanFactory;

    public static void main(String[] args) {
        SpringApplication.run(WebClientApplication.class, args);
    }

    @Scheduled(fixedRate = 5000)
    public void getUser() {
        UserService userService = beanFactory.getBean(UserService.class);
        System.out.println(userService.getUser());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
