package com.amazecode.eureka.provider.controller;

import com.amazecode.eureka.provider.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/4 16:46
 */
@RestController
public class EurekaProviderController {

    /**
     *   获取服务名称
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     *   获取服务端口号
     */
    @Value("${server.port}")
    private String port;

    @Autowired
    private HealthService healthService;

    /**
     * @description: 获取服务信息
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/4 16:50
     */
    @GetMapping("/getServer")
    public String getServerMsg(){
        return applicationName +":"+port;
    }
    
    /**
     * @description: 获取服务状态
     * @param: status
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/4 16:58
     */
    @GetMapping("/health")
    public String health(@RequestParam("status") Boolean status){
        healthService.setStatus(status);
        return healthService.getStatus();
    }
}
