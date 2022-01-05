package com.eureka.consumer.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/5 9:24
 */
@RestController
public class ConsumerController {

    @Autowired
    DiscoveryClient discoveryClient;

    @Qualifier("eurekaClient")
    @Autowired
    EurekaClient eurekaClient;

    @Autowired
    LoadBalancerClient lb;

    /**
     * @description: 获取服务列表
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/5 9:29
     */
    @GetMapping("/client")
    public String client() {
        List<String> services = discoveryClient.getServices();

        for (String s : services) {
            System.out.println(s);
        }

        // 拿到生产者实例信息
        List<ServiceInstance> instances = discoveryClient.getInstances("eureka-provider");

        for (ServiceInstance instance : instances) {
            System.out.println("serviceId:" + instance.getServiceId() +" uri:" + instance.getUri() + " port:" + instance.getPort());
        }
        return "services";
    }

    /**
     * @description: 手动实现生产者服务调用,不能实现自动轮询生产者服务不灵活
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/5 10:10
     */
    @GetMapping("/server")
    public String remoteClient () {

        // 使用服务名，找列表
        List<InstanceInfo> instances = eurekaClient.getInstancesByVipAddress("eureka-provider", false);

        if(instances.size()>0) {
            // 服务 指定第一个服务
            InstanceInfo instanceInfo = instances.get(0);
            if(instanceInfo.getStatus() == InstanceInfo.InstanceStatus.UP) {
                String url = "http://" + instanceInfo.getHostName() +":"+ instanceInfo.getPort() + "/getServer";
                System.out.println("url:" + url);
                RestTemplate restTemplate = new RestTemplate();
                // 请求client
                String respStr = restTemplate.getForObject(url, String.class);
                System.out.println("respStr:"  + respStr);
            }
        }
        return "xxoo";
    }

    private AtomicInteger atomicInteger = new AtomicInteger();

    /**
     * @description: 手动实现访问远程服务负载均衡
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/5 10:51
     */
    @GetMapping("/server1")
    public String remoteClient1 () {

        // 使用服务名，找列表
        List<InstanceInfo> instances = eurekaClient.getInstancesByVipAddress("eureka-provider", false);

        if(instances.size()>0) {
            /*
                自定义轮询算法
             */
            // 随机
            int serverIndex;
            //serverIndex = new Random().nextInt(instances.size());
            if (atomicInteger.get() > 1){
                atomicInteger.set(0);
            }
            // 轮训
            int i = atomicInteger.getAndIncrement();
            serverIndex = i % instances.size();

            // 更具自定义算法获取生产者服务
            InstanceInfo instanceInfo = instances.get(serverIndex);
            if(instanceInfo.getStatus() == InstanceInfo.InstanceStatus.UP) {
                String url = "http://" + instanceInfo.getHostName() +":"+ instanceInfo.getPort() + "/getServer";
                System.out.println("url:" + url);
                RestTemplate restTemplate = new RestTemplate();
                // 请求client
                String respStr = restTemplate.getForObject(url, String.class);
                System.out.println("respStr:"  + respStr);
            }
        }
        return "xxoo";
    }

    /**
     * @description: 默认轮询生产者,但是需要手动拼接url
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/5 10:18
     */
    @GetMapping("/server2")
    public String remoteClient2 () {

        // 获取生产者实例(默认轮询方式负载均衡)
        ServiceInstance instance = lb.choose("eureka-provider");

        String url ="http://" + instance.getHost() +":"+ instance.getPort() + "/getServer";

        RestTemplate restTemplate = new RestTemplate();

        String respStr = restTemplate.getForObject(url, String.class);

        System.out.println(respStr);

        return respStr;
    }
}
