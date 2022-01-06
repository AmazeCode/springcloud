package com.amazecode.userconsumer.api.fallback;

import com.amazecode.userconsumer.api.ConsumerApi;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 熔断处理
 * @Author: zhangyadong
 * @Date: 2022/1/6 9:33
 */
@Component // 需要交给spring管理
public class ConsumerBack implements FallbackFactory<ConsumerApi> {

    @Override
    public ConsumerApi create(Throwable throwable) {

        return new ConsumerApi() {

            /*
                走降级的两种场景：
                1、调用的服务内部报错，直接走降级
                2、调用的如果是单个服务，服务宕机，会在超时重试时间阈值后，走服务降级
             */
            @Override
            public String isAlive() {
                return "服务降级了。。。。。。。。。。。。";
            }
        };
    }
}
