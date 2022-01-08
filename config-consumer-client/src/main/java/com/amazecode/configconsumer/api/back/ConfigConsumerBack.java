package com.amazecode.configconsumer.api.back;

import com.amazecode.configconsumer.api.ConfigConsumerApi;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/7 9:59
 */
@Component //注入到spring容器,以便@FeginClient配置时能够拿到
public class ConfigConsumerBack implements FallbackFactory<ConfigConsumerApi> {

    @Override
    public ConfigConsumerApi create(Throwable throwable) {
        return new ConfigConsumerApi(){
            /*
                走降级的两种场景：
                1、调用的服务内部报错，直接走降级
                2、调用的如果是单个服务，服务宕机，会在超时重试时间阈值后，走服务降级
             */
            @Override
            public String isAlive() {
                return "isAlive()服务降级了。。。。。。。。";
            }
        };
    }
}
