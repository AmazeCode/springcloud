package com.amazecode.userapi.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户接口
 *
 * com.amazecode.userconsumer.api.ConsumerApi#isAlive()
 * to {GET /user/isAlive}: There is already 'consumerBack' bean method
 * 添加fegin、hystrix后启动报的错误，这个算是fegin的一个坑，在api中添加了@RequestMapping(“/xxx”)注解后，
 * SpringMVC、Hystrix、Feign都要检查uri。如果单是Feign，那么加@RequestMapping没问题，但是如果再配上Hystrix，就会重复注册两次url。
 *
 * 解决方法：
 * 方法1：去掉@RequestMapping(“/xxx”)
 * 方法2：在每个方法前手写uri（如果不嫌麻烦的话…），或者定义一个常量去拼接，例如：@GetMapping("/xxx/xxx")
 */
//@RequestMapping("/user")
public interface RegisterApi {

    @GetMapping("/user/isAlive")
    public String isAlive();
}
