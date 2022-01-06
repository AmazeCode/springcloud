package com.amazecode.userconsumer.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义Fegin配置
 * @Author: zhangyadong
 * @Date: 2022/1/6 9:39
 */
@Configuration
public class FeginConfig {

    /**
     * 自定义fegin日志级别
     * 配置文件需要开启日志支持
     * # 设置fegin打印日志等级
     * logging.level.com.amazecode.userconsumer:debug
     */
    @Bean
    public Logger.Level logLevel(){
        return Logger.Level.BASIC;
    }
}
