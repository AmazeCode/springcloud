package com.amazecode.userconsumer.controller;

import com.amazecode.userconsumer.api.ConsumerApi;
import com.amazecode.userconsumer.api.OtherSystemApi;
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

    @Autowired
    OtherSystemApi otherSystemApi;

    /**
     * @description: fegin调用系统内部服务
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/6 8:57
     */
    @GetMapping("/getAlive")
    public String getAlive() {
        System.out.println("user-consumer执行了.................");
        return consumerApi.isAlive();
    }

    /**
     * @description: fegin调用第三方系统接口
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/6 9:00
     */
    @GetMapping("/thirdApi")
    public String getThirdApi() {
        String data = otherSystemApi.getData();
        return data;
    }
}
