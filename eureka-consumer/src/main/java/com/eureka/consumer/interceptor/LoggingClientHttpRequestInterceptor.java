package com.eureka.consumer.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/5 9:17
 */
public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    /**
     * 基于spring-cloud-starter-loadbalancer的负载均衡,没有引入ribbion时默认采用loadbalancer进行负载
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        System.out.println("拦截了");
        System.out.println(request.getURI());

        ClientHttpResponse response = execution.execute(request, body);
        System.out.println(response.getHeaders());

        return response;
    }
}
