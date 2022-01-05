package com.amazecode.userconsumer.api;

import com.amazecode.userapi.api.RegisterApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 不结合eureka，就是自定义一个client名字。就用url属性指定 服务器列表。url=“http://ip:port/”
 * @Author: zhangyadong
 * @Date: 2022/1/5 18:47
 */
@FeignClient(name = "user-provider")
public interface ConsumerApi extends RegisterApi {

}
