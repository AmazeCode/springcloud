package com.amazecode.userapi.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户接口
 * @Author: zhangyadong
 * @Date: 2022/1/5 17:57
 */
@RequestMapping("/user")
public interface RegisterApi {

    @GetMapping("/isAlive")
    public String isAlive();
}
