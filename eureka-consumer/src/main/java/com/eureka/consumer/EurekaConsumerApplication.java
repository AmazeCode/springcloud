package com.eureka.consumer;

import com.eureka.consumer.interceptor.LoggingClientHttpRequestInterceptor;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RoundRobinRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class EurekaConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaConsumerApplication.class, args);
    }

    @Bean
    @LoadBalanced // 添加负载均衡注解
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new LoggingClientHttpRequestInterceptor());
        return restTemplate;
    }

    /*
        优先级别高于,配置文件指定服务的负载策略
        eureka-provider.ribbon.NFLoadBalancerRuleClassName=com.netflix.loadbalancer.RandomRule
     */
    @Bean
    public IRule myRule(){
        // 轮询
        return new RoundRobinRule();
        // 随机
        //return new RandomRule();
    }
}
