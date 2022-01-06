package com.amazecode.userconsumer.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 访问第三方系统接口方式
 * @Author: zhangyadong
 * @Date: 2022/1/6 8:52
 */
@FeignClient(name = "other-system-api",url = "http://129.28.192.214:8888/")
public interface OtherSystemApi {

    /**
     * 访问第三方服务返回数据
     */
    @GetMapping("/songList")
    String getData();
}
