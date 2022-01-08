package com.amazecode.configconsumer.api;

import com.amazecode.configconsumer.api.back.ConfigConsumerBack;
import com.amazecode.userapi.api.RegisterApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/7 9:58
 */
@FeignClient(name = "user-provider", fallbackFactory = ConfigConsumerBack.class)
public interface ConfigConsumerApi extends RegisterApi {
}
