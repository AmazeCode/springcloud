package com.amazecode.userconsumer.controller;

import com.amazecode.userconsumer.api.ConsumerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/5 18:35
 */
@RestController
public class ConsumerController {

    @Autowired
    ConsumerApi consumerApi;

    @GetMapping("/getAlive")
    public String getAlive() {
        System.out.println("user-consumer执行了.................");
        return consumerApi.isAlive();
    }
}
