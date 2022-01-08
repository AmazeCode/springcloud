package com.amazecode.configconsumer.controller;

import com.amazecode.configconsumer.api.ConfigConsumerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/7 9:58
 */
@RestController
@RefreshScope // 配置变更动态刷新
public class ConfigConsumerController {

    @Autowired
    ConfigConsumerApi configConsumerApi;

    /**
     * @description: fegin调用系统内部服务
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/6 8:57
     */
    @GetMapping("/getAlive")
    public String getAlive() {
        System.out.println("config-consumer执行了.................");
        return "config-" + configConsumerApi.isAlive();
    }

    /**
     * 从配置中心获取配置
     */
    @Value("${myconfig}")
    String myconfig;

    /**
     * 从配置中心获取配置
     */
    @GetMapping("/config")
    public String myConfig() {
        return myconfig;
    }
}
