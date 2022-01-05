package com.amazecode.userprovider.controller;

import com.amazecode.userapi.api.RegisterApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/5 18:13
 */
@RestController
public class UserController implements RegisterApi {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String port;

    @Override
    public String isAlive() {
        System.out.println("调用了。。。。。。。。。。。");
        return applicationName + "---" + port;
    }
}
