package com.eureka.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/5 10:26
 */
@RestController
public class ConsumerController1 {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    DiscoveryClient discoveryClient;

    /**
     * @description: 正常调用服务
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/5 17:06
     */
    @GetMapping("/rest-server")
    public String getServer() {

        // 自动处理URL
        String url ="http://eureka-provider/getServer";

        String respStr = restTemplate.getForObject(url, String.class);

        System.out.println(respStr);

        return respStr;
    }

    /**
     * @description: 调用配置的未被注册中心管理的服务
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/5 17:06
     */
    @GetMapping("/rest-server1")
    public String getServer1() {

        // 读取未被注册中心管理的服务,xxx可为任意值,仅仅占位
        String url ="http://xxx/getServer";

        String respStr = restTemplate.getForObject(url, String.class);

        System.out.println(respStr);

        return respStr;
    }
}
