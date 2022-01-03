package com.amazecode.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer // 标记为Eureka 服务
@SpringBootApplication
public class EurekaServerEuk1Application {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerEuk1Application.class, args);
    }

}
