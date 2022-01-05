

# springcloud全家桶

## 服务注册与发现

#### 注册中心和微服务间的关系

> ![eureka-server-client](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\eureka-server-client.png) 《服务注册与发现关系图》

#### client功能

1. 注册：每个微服务启动时，将自己的网络地址等信息注册到注册中心，注册中心会存储（内存中）这些信息。
2. 获取服务注册表：服务消费者从注册中心，查询服务提供者的网络地址，并使用该地址调用服务提供者，为了避免每次都查注册表信息，所以client会定时去server拉取注册表信息到缓存到client本地。
3. 心跳：各个微服务与注册中心通过某种机制（心跳）通信，若注册中心长时间和服务间没有通信，就会注销该实例。
4. 调用：实际的服务调用，通过注册表，解析服务名和具体地址的对应关系，找到具体服务的地址，进行实际调用。

#### server注册中心功能

1. 服务注册表：记录各个微服务信息，例如服务名称，ip，端口等。

   注册表提供 查询API（查询可用的微服务实例）和管理API（用于服务的注册和注销）。

2. 服务注册与发现：注册：将微服务信息注册到注册中心。发现：查询可用微服务列表及其网络地址。

3. 服务检查：定时检测已注册的服务，如发现某实例长时间无法访问，就从注册表中移除。

组件：Eureka , Consul , ZooKeeper，nacos等。

### Eureka 单节点搭建

1、pom.xml

```java
<!--有的教程中还引入spring-boot-starter-web，其实不用。因为上面的依赖已经包含了它。在pom中点此依赖进去，一共点4次spring-cloud-netflix-eureka-server，发现web的依赖。 
    -->
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

2、application.properties

```sh
# 是否讲自己注册到注册中心，默认true，由于当前是注册中心server，故设置成false，表明该服务不会向注册中心注册
eureka.client.register-with-eureka=false
# 是否从注册中心获取注册信息,由于单节点不需要同步其他节点数据，设置为false
eureka.client.fetch-registry=false
#设置服务注册中心的URL，用于client和server端交流
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/
#关闭自我保护
eureka.server.enable-self-preservation=false
```

3、启动类上添加注解

```java
// 启动类上添加此注解标识该服务为配置中心
@EnableEurekaServer
```

4、PS：Eureka会暴露一些端点。端点用于Eureka Client注册自身，获取注册表，发送心跳。

5、简单看一下eureka server控制台，实例信息区，运行环境信息区，Eureka Server自身信息区。

![eureka-server-dashboard](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\eureka-server-dashboard.png)

### Eureka 高可用

1、准备

准备2个节点部署eureka，也可以单机部署

修改本机host文件，绑定一个主机名，单机部署时使用ip地址会有问题

```
127.0.0.1 ek1.com
127.0.0.1 ek2.com
```

2、新建两个项目，引入依赖

```java
 <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <!-- 权限依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <!-- 监控依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

3、application.properties配置文件

节点1：

```
# 设置服务名
spring.application.name=EurekaServer
#web端口，服务是由这个端口处理rest请求的
server.port=7901

#是否将自己注册到其他Eureka Server,默认为true 需要
eureka.client.register-with-eureka=true
#是否从eureka server获取注册信息， 需要
eureka.client.fetch-registry=true
# 此节点应向其他节点发起请求,带上认证用户密码（设置服务注册中心的URL，用于client和server端交流）
eureka.client.service-url.defaultZone=http://admin:admin@ek2.com:7902/eureka/
#主机名，必填
eureka.instance.hostname=ek1.com

# 是否actuator端点启用和暴露，  启用
#management.endpoint.shutdown.enabled=true
# * 可以用来表示所有的端点，例如，通过HTTP公开所有的端点，除了env和beans端点
management.endpoints.web.exposure.include=*
#management.endpoints.web.exposure.exclude=env,beans

# 自定义元数据,服务内可获取,可被其他微服务读取到
eureka.instance.metadata-map.dalao=xiaoming1
#关闭自我保护
#eureka.server.enable-self-preservation=false

#安全认证
spring.security.user.name=admin
spring.security.user.password=admin

```

节点2：

```
# 设置服务名
spring.application.name=EurekaServer
#web端口，服务是由这个端口处理rest请求的
server.port=7902

#是否将自己注册到其他Eureka Server,默认为true 需要
eureka.client.register-with-eureka=true
#是否从eureka server获取注册信息， 需要
eureka.client.fetch-registry=true
# 此节点应向其他节点发起请求,带上认证用户密码（设置服务注册中心的URL，用于client和server端交流）
eureka.client.service-url.defaultZone=http://admin:admin@ek1.com:7901/eureka/
#主机名，必填
eureka.instance.hostname=ek2.com

# 是否actuator端点启用和暴露，  启用
#management.endpoint.shutdown.enabled=true
# * 可以用来表示所有的端点，例如，通过HTTP公开所有的端点，除了env和beans端点
management.endpoints.web.exposure.include=*
#management.endpoints.web.exposure.exclude=env,beans

# 自定义元数据,服务内可获取,可被其他微服务读取到
eureka.instance.metadata-map.dalao=xiaoming2
#关闭自我保护
#eureka.server.enable-self-preservation=false

#安全认证
spring.security.user.name=admin
spring.security.user.password=admin
```

4、开启权限认证，需要关闭csrf认证，否则不生效

```
@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();// 关闭csrf
        http.authorizeRequests().anyRequest().authenticated().and().httpBasic();//开启认证
    }
}
```

![eureka-security-login](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\eureka-security-login.png)

![eureka-server-client](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\eureka-server-cluster.png)

5、监控信息查看

http://localhost:7902/actuator

```
{
    "_links":{
        "self":{
            "href":"http://localhost:7902/actuator",
            "templated":false
        },
        "beans":{
            "href":"http://localhost:7902/actuator/beans",
            "templated":false
        },
        "caches-cache":{
            "href":"http://localhost:7902/actuator/caches/{cache}",
            "templated":true
        },
        "caches":{
            "href":"http://localhost:7902/actuator/caches",
            "templated":false
        },
        "health":{
            "href":"http://localhost:7902/actuator/health",
            "templated":false
        },
        "health-path":{
            "href":"http://localhost:7902/actuator/health/{*path}",
            "templated":true
        },
        "info":{
            "href":"http://localhost:7902/actuator/info",
            "templated":false
        },
        "conditions":{
            "href":"http://localhost:7902/actuator/conditions",
            "templated":false
        },
        "configprops":{
            "href":"http://localhost:7902/actuator/configprops",
            "templated":false
        },
        "configprops-prefix":{
            "href":"http://localhost:7902/actuator/configprops/{prefix}",
            "templated":true
        },
        "env":{
            "href":"http://localhost:7902/actuator/env",
            "templated":false
        },
        "env-toMatch":{
            "href":"http://localhost:7902/actuator/env/{toMatch}",
            "templated":true
        },
        "loggers":{
            "href":"http://localhost:7902/actuator/loggers",
            "templated":false
        },
        "loggers-name":{
            "href":"http://localhost:7902/actuator/loggers/{name}",
            "templated":true
        },
        "heapdump":{
            "href":"http://localhost:7902/actuator/heapdump",
            "templated":false
        },
        "threaddump":{
            "href":"http://localhost:7902/actuator/threaddump",
            "templated":false
        },
        "metrics-requiredMetricName":{
            "href":"http://localhost:7902/actuator/metrics/{requiredMetricName}",
            "templated":true
        },
        "metrics":{
            "href":"http://localhost:7902/actuator/metrics",
            "templated":false
        },
        "scheduledtasks":{
            "href":"http://localhost:7902/actuator/scheduledtasks",
            "templated":false
        },
        "mappings":{
            "href":"http://localhost:7902/actuator/mappings",
            "templated":false
        },
        "refresh":{
            "href":"http://localhost:7902/actuator/refresh",
            "templated":false
        },
        "features":{
            "href":"http://localhost:7902/actuator/features",
            "templated":false
        },
        "serviceregistry":{
            "href":"http://localhost:7902/actuator/serviceregistry",
            "templated":false
        }
    }
}
```

eureka高可用服务搭建完成

#### Eureka-Provider(生产者)

1、创建项目,maven依赖

```
<!-- springboot web依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- eureka客户端依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <!-- 用来上报节点信息 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

2、application.properties配置文件

```
# 服务名
spring.application.name=eureka-provider
# 端口号
server.port=8091

# 注册中心服务地址
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/
# 元数据
eureka.instance.metadata-map.dalao=xiaojun
# 开放所有站点
management.endpoints.web.exposure.include=*
# 可以远程关闭服务节点
management.endpoint.shutdown.enabled=true
# 可以上报服务的真实健康状态
eureka.client.healthcheck.enabled=true
```

3、java代码

servcie：

```java
@Service
public class HealthService implements HealthIndicator {

    private Boolean status = true;

    public String getStatus() {
        return status.toString();
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public Health health() {
        if (status) {
            // 标记上线
            return new Health.Builder().up().build();
        }
        // 设置下线
        return new Health.Builder().down().build();
    }
}
```

controller：

```java
@RestController
public class EurekaProviderController {

    /**
     *   获取服务名称
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     *   获取服务端口号
     */
    @Value("${server.port}")
    private String port;

    @Autowired
    private HealthService healthService;

    /**
     * @description: 获取服务信息
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/4 16:50
     */
    @GetMapping("/getServer")
    public String getServerMsg(){
        return applicationName +":"+port;
    }
    
    /**
     * @description: 获取服务状态
     * @param: status
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/4 16:58
     */
    @GetMapping("/health")
    public String health(@RequestParam("status") Boolean status){
        healthService.setStatus(status);
        return healthService.getStatus();
    }
}
```

如果测试负载均衡可以按照同样的方式创建其他provider，修改服务名端口即可

#### eureka-consumer消费者

1、引入pom依赖

```
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- eureka客户端依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <!-- 用来上报节点信息 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

2、设置application.properties配置文件

```
spring.application.name=eureka-consumer
server.port=9090
# 注册服务（默认true）
#eureka.client.register-with-eureka=true
# 拉取服务列表 （默认true）
#eureka.client.fetch-registry=true
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/
```

3、通过服务名端口号访问服务(不灵活)

```java
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

        return respStr;
    }
}
```

4、通过服务名访问服务，配置负载均衡策略

启动类注入RestTemplate

```java
@Bean
    @LoadBalanced// 添加负载均衡注解，默认轮询方式访问
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new LoggingClientHttpRequestInterceptor());
        return restTemplate;
    }
```

依赖LoggingClientHttpRequestInterceptor

```java
/**
 * @Author: zhangyadong
 * @Date: 2022/1/5 9:17
 */
public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    /**
     * @description: 请求拦截
     * @param: request
     * @param: body
     * @param: execution
     * @return: org.springframework.http.client.ClientHttpResponse
     * @author: zhangyadong
     * @date: 2022/1/5 9:44
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
```

**切换负载均衡策略(Ribbon)：**

一、application.properties添加配置（优先级高于@LoadBalanced）

```java
# 针对于eureka-provider服务名的ribbon策略(优先级高于@LoadBalancer默认轮询的负载策略) 轮询-RoundRobinRule 随机-RandomRule
eureka-provider.ribbon.NFLoadBalancerRuleClassName=com.netflix.loadbalancer.RandomRule
```

二、添加自定义Bean，这里为了简便在启动类添加

```java
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
```

**调用外部服务**

```sh
# 先禁用从eureka列表读取服务列表(forbid-provider为自定义服务名称)
ribbon.eureka.enabled=false
# 配置未注册服务(不配置具体的服务,可针对全部服务名称)
ribbon.listOfServers=localhost:8091,localhost:8092
```

添加接口测试

```java
 /**
     * @description: 调用配置的未被注册中心管理的服务
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/5 17:06
     */
    @GetMapping("/rest-server1")
    public String getServer1() {

        // 读取未被注册中心管理的服务,xxx可为任意值,仅仅占位
        String url ="http://xxx/getServer";

        String respStr = restTemplate.getForObject(url, String.class);

        System.out.println(respStr);

        return respStr;
    }
}
```

*注意点：*Hoxton.SR3之后版本使用ribbon需要手动添加ribbon依赖

```java
<!-- 稳定版本 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.6.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
<properties>
    <java.version>1.8</java.version>
    <spring-cloud.version>Hoxton.SR3</spring-cloud.version>
</properties>
```



----



### Fegin以及Hysteria应用

##### Fegin和OpenFegin的关系

Feign本身不支持Spring MVC的注解，它有一套自己的注解

OpenFeign是Spring Cloud 在Feign的基础上支持了Spring MVC的注解，如@RequesMapping等等。
OpenFeign的`@FeignClient`可以解析SpringMVC的@RequestMapping注解下的接口，
并通过动态代理的方式产生实现类，实现类中做负载均衡并调用其他服务。

1、创建项目user-api

依赖 spring-boot-starter-web

```java
package com.amazecode.userapi.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户接口
 * @Author: zhangyadong
 * @Date: 2022/1/5 17:57
 */
@RequestMapping("/user")
public interface RegisterApi {

    @GetMapping("/isAlive")
    public String isAlive();
}
```

2、创建生产服务user-provider

引入user-api依赖,并实现接口

```java
<dependency>
            <groupId>com.amazecode</groupId>
            <artifactId>user-api</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```

配置文件配置

```java
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/
server.port=7971
spring.application.name=user-provider
```

实现引入的user-api接口

```java
package com.amazecode.userprovider.controller;

import com.amazecode.userapi.api.RegisterApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/5 18:13
 */
@RestController
public class UserController implements RegisterApi {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String port;

    @Override
    public String isAlive() {
        System.out.println("调用了。。。。。。。。。。。");
        return applicationName + "---" + port;
    }
}

```

3、创建消费服务consumer-provider

引入必要依赖以及user-api依赖

```java
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- HttpClient 实现 -->
        <dependency>
            <groupId>io.github.openfeign</groupId>
            <artifactId>feign-httpclient</artifactId>
        </dependency>

        <!-- 引入自定义user-api -->
        <dependency>
            <groupId>com.amazecode</groupId>
            <artifactId>user-api</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
```

设置配置文件

```java
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/
server.port=7981
spring.application.name=user-consumer

#连接超时时间(ms)
ribbon.ConnectTimeout=1000
#业务逻辑超时时间(ms)
ribbon.ReadTimeout=2000
#同一台实例最大重试次数,不包括首次调用
ribbon.MaxAutoRetries=3
#重试负载均衡其他的实例最大重试次数,不包括首次调用
ribbon.MaxAutoRetriesNextServer=3
#是否所有操作都重试
ribbon.OkToRetryOnAllOperations=false
```

启动类添加Fegin注解

```java
@EnableFeignClients 
```

调用user-api接口

api

```java
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
```

controller

```java
package com.amazecode.userconsumer.controller;

import com.amazecode.userconsumer.api.ConsumerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/5 18:35
 */
@RestController
public class ConsumerController {

    @Autowired
    ConsumerApi consumerApi;

    @GetMapping("/getAlive")
    public String getAlive() {
        System.out.println("user-consumer执行了.................");
        return consumerApi.isAlive();
    }
}

```

启动注册中心、user-provider、user-consumer测试fegin方式是否能调通

*注意点：@FeginClient引入的是OpenFeign依赖，Fegin不支持SpringMvc注解*

**如果user-provider开启了权限认证，消费端如何进行访问配置**

两种方式：自定义配置类，增加拦截器

（1）、自定义配置

```java
配置类：
public class FeignAuthConfiguration {
	
	@Bean
	public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
		return new BasicAuthRequestInterceptor("root", "root");
	}
}

在feign上加配置
@FeignClient(name = "user-provider",configuration = FeignAuthConfiguration.class)
```

小结：如果在配置类上添加了@Configuration注解，并且该类在@ComponentScan所扫描的包中，那么该类中的配置信息就会被所有的@FeignClient共享。最佳实践是：不指定@Configuration注解（或者指定configuration，用注解忽略），而是手动：

@FeignClient(name = "user-provider",configuration = FeignAuthConfiguration.class)

(2)、拦截器

```java
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class MyBasicAuthRequestInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate template) {
		// TODO Auto-generated method stub
		template.header("Authorization", "Basic cm9vdDpyb290");
	}
}

feign:
  client: 
    config:  
      service-valuation: 
        request-interceptors:
        - com.amazecode.interceptor.MyBasicAuthRequestInterceptor
```

## 原理

1. 主程序入口添加@EnableFeignClients注解开启对Feign Client扫描加载处理。根据Feign Client的开发规范，定义接口并加@FeignClient注解。
2. 当程序启动时，会进行包扫描，扫描所有@FeignClient注解的类，并将这些信息注入Spring IoC容器中。当定义的Feign接口中的方法被调用时，通过JDK的代理方式，来生成具体的RequestTemplate。当生成代理时，Feign会为每个接口方法创建一个RequestTemplate对象，该对象封装了HTTP请求需要的全部信息，如请求参数名、请求方法等信息都在这个过程中确定。
3. 然后由RequestTemplate生成Request，然后把这个Request交给client处理，这里指的Client可以是JDK原生的URLConnection、Apache的Http Client，也可以是Okhttp。最后Client被封装到LoadBalanceClient类，这个类结合Ribbon负载均衡发起服务之间的调用。