package com.amazecode.configcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer // 启动配置服务注解
public class ConfigCenterApplication {

    /*
        获取配置规则：根据前缀匹配
        /{name}-{profiles}.properties
        /{name}-{profiles}.yml
        /{name}-{profiles}.json
        /{label}/{name}-{profiles}.yml

        name 服务名称
        profile 环境名称，开发、测试、生产：dev qa prd
        lable 仓库分支、默认master分支

        匹配原则：从前缀开始。 命名一定如"xx-bb.properties"格式
     */
    public static void main(String[] args) {
        SpringApplication.run(ConfigCenterApplication.class, args);
    }

}
