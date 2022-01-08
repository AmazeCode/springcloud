package com.amazecode.configconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient // 可让注册中心发现，适用范围广，不仅限于eureka注册中心
@EnableFeignClients // 启用Fegin远程调用注解
@EnableCircuitBreaker // 启用Hystrix注解
@EnableHystrixDashboard // 启用Hystrix管理页面
public class ConfigConsumerClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigConsumerClientApplication.class, args);
    }

}
