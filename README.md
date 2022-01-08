

# springcloud全家桶

## 服务注册与发现

#### 注册中心和微服务间的关系

> ![eureka-server-clien](img/eureka-server-client.png) 《服务注册与发现关系图》

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



### Fegin

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

# fegin默认支持ribbon,Ribbon的重试机制和Fegin重试机制有冲突，所有源码中默认关闭Fegin的重试机制，使用Ribbon的重试机制
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

**Fegin调用第三方系统接口方式**

1、创建调用第三方接口的fegin，注解标注第三方接口url，注意和调用服务配置的区别

*调用系统内部接口配置：@FeignClient(name = "user-provider")*

*调用第三方接口配置：@FeignClient(name = "other-system-api",url = "http://ip:8888/")*

```java
package com.amazecode.userconsumer.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 访问第三方系统接口方式
 * @Author: zhangyadong
 * @Date: 2022/1/6 8:52
 */
@FeignClient(name = "other-system-api",url = "http://ip:8888/")
public interface OtherSystemApi {

    /**
     * 访问第三方服务返回数据
     */
    @GetMapping("/songList")
    String getData();
}
```

2、Controller正常引入调用即可

```java
package com.amazecode.userconsumer.controller;

import com.amazecode.userconsumer.api.ConsumerApi;
import com.amazecode.userconsumer.api.OtherSystemApi;
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

    @Autowired
    OtherSystemApi otherSystemApi;

    /**
     * @description: fegin调用系统内部服务
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/6 8:57
     */
    @GetMapping("/getAlive")
    public String getAlive() {
        System.out.println("user-consumer执行了.................");
        return consumerApi.isAlive();
    }

    /**
     * @description: fegin调用第三方系统接口
     * @param:
     * @return: java.lang.String
     * @author: zhangyadong
     * @date: 2022/1/6 9:00
     */
    @GetMapping("/thirdApi")
    public String getThirdApi() {
        String data = otherSystemApi.getData();
        return data;
    }
}
```

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

**开启日志配置(user-consumer)**

```
# 设置fegin打印日志等级
logging.level.com.amazecode.userconsumer:debug
```

**重写日志等级(user-consumer)**

```java
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
```

-----

### Hystrix

**服务熔断**

服务熔断的作用类似于我们家用的保险丝，当某服务出现不可用或响应超时的情况时，为了防止整个系统出现雪崩，暂时停止对该服务的调用。

**服务降级**

服务降级是从整个系统的负荷情况出发和考虑的，对某些负荷会比较高的情况，为了预防某些功能（业务场景）出现负荷过载或者响应慢的情况，在其内部暂时舍弃对一些非核心的接口和数据的请求，而直接返回一个提前准备好的fallback（退路）错误处理信息。这样，虽然提供的是一个有损的服务，但却保证了整个系统的稳定性和可用性。

**熔断VS降级**

​	相同点：

​		※ 目标一致 都是从可用性和可靠性出发，为了防止系统崩溃；

 	   ※ 用户体验类似 最终都让用户体验到的是某些功能暂时不可用；

​    不同点：

​	触发原因不同 服务熔断一般是某个服务（下游服务）故障引起，而服务降级一般是从整体负荷考虑；

代码:(为了简单点，继续在原有项目user-consumer上进行新增)

**1、添加Hystrix依赖**

```java
<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
```

**2、开启配置**

```java
# 开启熔断
feign.hystrix.enabled=true
management.endpoints.web.exposure.include=*
# 隔离策略，默认是Thread, 可选:线程隔离-Thread｜信号量隔离-Semaphore
hystrix.command.default.execution.isolation.strategy=SEMAPHORE
```

**3、启动类开启注解**

```java
@EnableCircuitBreaker // Hystrix注解
```

**4、修改ConsumerApi**

```java
package com.amazecode.userconsumer.api;

import com.amazecode.userapi.api.RegisterApi;
import com.amazecode.userconsumer.api.fallback.ConsumerBack;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 不结合eureka，就是自定义一个client名字。就用url属性指定 服务器列表。url=“http://ip:port/”
 * @Author: zhangyadong
 * @Date: 2022/1/5 18:47
 */
@FeignClient(name = "user-provider", fallback = ConsumerBack.class)
public interface ConsumerApi extends RegisterApi {

}
```

新增fallback处理

```java
package com.amazecode.userconsumer.api.fallback;

import com.amazecode.userconsumer.api.ConsumerApi;
import org.springframework.stereotype.Component;

/**
 * 熔断处理
 * @Author: zhangyadong
 * @Date: 2022/1/6 9:33
 */
@Component // 需要交给spring管理
public class ConsumerBack implements ConsumerApi {

    @Override
    public String isAlive() {
        return "降级了。。。。。。";
    }
}
```

另外一种写法(FallBackFactory)

```java
package com.amazecode.userconsumer.api;

import com.amazecode.userapi.api.RegisterApi;
import com.amazecode.userconsumer.api.fallback.ConsumerBack;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 不结合eureka，就是自定义一个client名字。就用url属性指定 服务器列表。url=“http://ip:port/”
 * @Author: zhangyadong
 * @Date: 2022/1/5 18:47
 */
@FeignClient(name = "user-provider", fallbackFactory = ConsumerBack.class)
public interface ConsumerApi extends RegisterApi {

}

```

```java
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
```

**Hystrix信号量隔离与线程隔离**

默认情况下hystrix使用线程池控制请求隔离

线程池隔离技术，是用 Hystrix 自己的线程去执行调用；而信号量隔离技术，是直接让 tomcat 线程去调用依赖服务。信号量隔离，只是一道关卡，信号量有多少，就允许多少个 tomcat 线程通过它，然后去执行。

信号量隔离主要维护的是Tomcat的线程，不需要内部线程池，更加轻量级。

配置项目详解：

```sh
hystrix.command.default.execution.isolation.strategy 隔离策略，默认是Thread, 可选Thread｜Semaphore
thread 通过线程数量来限制并发请求数，可以提供额外的保护，但有一定的延迟。一般用于网络调用
semaphore 通过semaphore count来限制并发请求数，适用于无网络的高并发请求
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds 命令执行超时时间，默认1000ms
hystrix.command.default.execution.timeout.enabled 执行是否启用超时，默认启用true
hystrix.command.default.execution.isolation.thread.interruptOnTimeout 发生超时是是否中断，默认true
hystrix.command.default.execution.isolation.semaphore.maxConcurrentRequests 最大并发请求数，默认10，该参数当使用ExecutionIsolationStrategy.SEMAPHORE策略时才有效。如果达到最大并发请求数，请求会被拒绝。理论上选择semaphore size的原则和选择thread size一致，但选用semaphore时每次执行的单元要比较小且执行速度快（ms级别），否则的话应该用thread。
semaphore应该占整个容器（tomcat）的线程池的一小部分。
```

**开启Hystrix-Dashboard**

启动类

```java
@EnableHystrixDashboard // 启用Hystrix注解
@EnableHystrixDashboard // 启用Hystrix管理页面
```

依赖

```java
<!-- hystrix-dashboard 依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>
                spring-cloud-starter-netflix-hystrix-dashboard
            </artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

开启配置：

```java
# 可以远程关闭服务节点
management.endpoint.shutdown.enabled=true
# 可以上报服务的真实健康状态
eureka.client.healthcheck.enabled=true
```

健康上报(向注册中心发送ping)

http://localhost:7981/actuator/hystrix.stream

Hystrix图形化监控页面

http://localhost:7981/hystrix

输入地址进行监控：

http://localhost:7981/actuator/hystrix.stream  然后点击Monitor Stream即可，想要有监控数据显示，需要调用下服务进行数据请求

### 链路追踪

如果能跟踪每个请求，中间请求经过哪些微服务，请求耗时，网络延迟，业务逻辑耗时等。我们就能更好地分析系统瓶颈、解决系统问题。因此链路跟踪很重要。

我们自己思考解决方案：在调用前后加时间戳。捕获异常。

链路追踪目的：解决错综复杂的服务调用中链路的查看。排查慢服务

```
zipkin,twitter开源的。是严格按照谷歌的Dapper论文来的。
pinpoint 韩国的 Naver公司的。
Cat 美团点评的
EagleEye 淘宝的
```

**链路追踪要考虑的几个问题**

1. 探针的性能消耗。尽量不影响 服务本尊。
2. 易用。开发可以很快接入，别浪费太多精力。
3. 数据分析。要实时分析。维度足够。

**Sleuth简介**

Sleuth是Spring cloud的分布式跟踪解决方案。

1. span(跨度)，基本工作单元。一次链路调用，创建一个span，

   span用一个64位id唯一标识。包括：id，描述，时间戳事件，spanId,span父id。

   span被启动和停止时，记录了时间信息，初始化span叫：root span，它的span id和trace id相等。

2. trace(跟踪)，一组共享“root span”的span组成的树状结构 称为 trace，trace也有一个64位ID，trace中所有span共享一个trace id。类似于一颗 span 树。

3. annotation（标签），annotation用来记录事件的存在，其中，核心annotation用来定义请求的开始和结束。

   - CS(Client Send客户端发起请求)。客户端发起请求描述了span开始。
   - SR(Server Received服务端接到请求)。服务端获得请求并准备处理它。SR-CS=网络延迟。
   - SS（Server Send服务器端处理完成，并将结果发送给客户端）。表示服务器完成请求处理，响应客户端时。SS-SR=服务器处理请求的时间。
   - CR（Client Received 客户端接受服务端信息）。span结束的标识。客户端接收到服务器的响应。CR-CS=客户端发出请求到服务器响应的总时间。

**使用**

**Sleuth和zipkin使用**

1、每个服务(user-consumer、user-provider)需要监控的系统都需要引入依赖

```java
<!-- 引入sleuth依赖 -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-sleuth</artifactId>
		</dependency>
    <!-- zipkin -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zipkin</artifactId>
        </dependency>
```

**zipkin**

zipkin是twitter开源的分布式跟踪系统。

原理收集系统的时序数据，从而追踪微服务架构中系统延时等问题。还有一个友好的界面。

由4个部分组成：

Collector、Storage、Restful API、Web UI组成

采集器，存储器，接口，UI。

原理：

sleuth收集跟踪信息通过http请求发送给zipkin server，zipkin将跟踪信息存储，以及提供RESTful API接口，zipkin ui通过调用api进行数据展示。

默认内存存储，可以用mysql，ES等存储。

2、添加配置

```java
# 链路追踪
spring.zipkin.base-url=http://localhost:9411
# 采样比例
spring.sleuth.sampler.rate=1
```

3、启动zipkin。

```
官网地址：https://zipkin.io/pages/quickstart.html
jar包下载：curl -sSL https://zipkin.io/quickstart.sh | bash -s
我放到了 目录：D:\  下面。
java -jar zipkin-server-2.23.16-exec.jar
或者docker：
docker run -d -p 9411:9411 openzipkin/zipkin
```

![image-20220106154253652](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\zipkin-server.png)

zipkin管理页面

http://localhost:9411/

![image-20220106160129254](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\zipkin-admin.png)

### Admin健康检查

1、添加依赖(**user-consumer**)

```java
<!-- admin -->
        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-client</artifactId>
            <version>2.2.1</version>
        </dependency>
```

2、添加admin配置

```java
# Admin 健康检查
management.endpoint.health.show-details=always
# admin 服务
spring.boot.admin.client.url=http://localhost:8080
```

3、创建admin服务

新建springboot项目admin，引入依赖

```java
server端：
<!-- Spring Cloud Admin -->
		<dependency>
			<groupId>de.codecentric</groupId>
			<artifactId>spring-boot-admin-starter-server</artifactId>
			<version>2.2.1</version>
		</dependency>
		<!-- Admin 界面 -->
		<dependency>
			<groupId>de.codecentric</groupId>
			<artifactId>spring-boot-admin-server-ui</artifactId>
			<version>2.2.1</version>
		</dependency>
```

添加配置文件

```java
spring.application.name=admin
# 应用服务web访问端口
server.port=8080
# ActuatorWeb访问端口
management.server.port=8081
management.endpoints.jmx.exposure.include=*
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

启动类

```java
@EnableAdminServer // 开启Admin服务
```

启动服务访问管理页面

http://localhost:8081

![image-20220106165327994](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\admin-manage.png)

*整合发送邮件发送邮件以及钉钉通知，参考admin模块代码*

### 网关

Starter阿里云镜像

https://start.aliyun.com/

**概念**

服务治理，服务注册发现，服务调用，熔断。已经学完。

微服务没有网关，会有下面的问题：

1. 客户端请求多个微服务，增加了客户端复杂性，每个微服务都要做用户认证，限流等，避免和多个微服务打交道的复杂性。

2. 有跨域问题，不在同一个域。

3. 认证复杂，每个服务都要独立认证，服务要求的权限不一致。

4. 难以重构。因为微服务被客户端调用着，重构难以实施。


网关是介于客户端（外部调用方比如app，h5）和微服务的中间层。

Zuul是Netflix开源的微服务网关，核心是一系列过滤器。这些过滤器可以完成以下功能。

1. 是所有微服务入口，进行分发。
2. 身份认证与安全。识别合法的请求，拦截不合法的请求。
3. 监控。在入口处监控，更全面。
4. 动态路由。动态将请求分发到不同的后端集群。
5. 压力测试。可以逐渐增加对后端服务的流量，进行测试。
6. 负载均衡。也是用ribbon。
7. 限流（望京超市）。比如我每秒只要1000次，10001次就不让访问了。
8. 服务熔断

网关和服务的关系：演员和剧场检票人员的关系。

zuul默认集成了：ribbon和hystrix。

1、新建项目（zull），引入依赖

```java
<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
		</dependency>
```

2、启动类

```java
@EnableZuulProxy
```

3、配置文件

```java
# 应用名称
spring.application.name=zuul
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/
server.port=80

# 路由端点 调试的时候，看网关请求的地址，以及 映射是否正确。网关请求有误时，可以通过此处排查错误。
#  开放所有站点
management.endpoints.web.exposure.include=*
# 健康详情
management.endpoint.health.show-details=always
# 开启健康检查
management.endpoint.health.enabled=true
# 开启路由
management.endpoint.routes.enabled=true
```

测试访问

网关会将服务名转换成具体服务的ip和端口，实际进行访问

```
http://localhost/user-consumer/getAlive
```

**配置指定微服务的访问路径**

1、通过指定服务名访问(虚拟主机名)

```java
# 通过服务名配置虚拟主机名(user-consumer为实际存在的服务名)
zuul.routes.user-consumer=/xxoo/**
```

访问方式：

```
http://localhost/xxoo/getAlive
```

2、自定义访问路径

```java
# 自定义映射路径
zuul.routes.xxs.path=/xx/**
zuul.routes.xxs.service-id=xx
zuul.routes.xxs.url=http://baidu.com
```

访问方式

```
http://localhost/xx/getAlive
```

**忽略微服务**

配置

```
zuul.ignored-services=user-provider
```

访问方式

```java
# 请求不通,拦截user-provider服务
http://localhost/api/v1/user-provider/user/isAlive
```

**前缀**

```
zuul.prefix=/api/v1
```

访问方式

```java
http://localhost/api/v1/xxoo/getAlive
```

带上前缀请求

```
zuul.strip-prefix=false
```

## 配置中心

### 为什么需要配置中心

单体应用，配置写在配置文件中，没有什么大问题。如果要切换环境 可以切换不同的profile（2种方式），但在微服务中。

1. 微服务比较多。成百上千，配置很多，需要集中管理。

2. 管理不同环境的配置。

3. 需要动态调整配置参数，更改配置不停服。

   

### 配置中心介绍

分布式配置中心包括3个部分：

1. 存放配置的地方：git ，本地文件 等。
2. config  server。从 1 读取配置。
3. config client。是 config server 的客户端 消费配置

### 配置服务搭建

**1、创建仓库**

![image-20220107092332654](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\gitee-config.png)

登录gitee创建仓库，并上传几个配置文件

**2、新建微服务作为配置中心服务(config-center)**

依赖

```java
<!-- eureka -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<!-- config中心依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

配置文件

```java
# 应用名称
spring.application.name=config-center
server.port=6900
# 指定配置地址
spring.cloud.config.server.git.uri=https://gitee.com/AmazeCode/config-center.git
# 指定分支
spring.cloud.config.label=master
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/
```

启动类

```java
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
```

**3、启动测试拉取**

http://localhost:6900/master/application-s.properties

拉取结果：(如果application.properties属性和application-s.properties默认会被拉取过来，属性一样则拉取的配置文件属性覆盖application.properties属性)

```
config.msg: "application-s"
message: "application"
```

#### 配置文件匹配规则

```
获取配置规则：根据前缀匹配
/{name}-{profiles}.properties
/{name}-{profiles}.yml
/{name}-{profiles}.json
/{label}/{name}-{profiles}.yml

name 服务名称
profile 环境名称，开发、测试、生产：dev qa prd
lable 仓库分支、默认master分支

匹配原则：从前缀开始。反例：config.properties匹配不到
```

### 配置中心客户端配置

新建config-consumer-client项目

**配置文件**

application.properties修改为bootstarp.properties

```java
# 应用名称
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/
server.port=6901
spring.application.name=user-consumer


# fegin默认支持ribbon,Ribbon的重试机制和Fegin重试机制有冲突，所有源码中默认关闭Fegin的重试机制，使用Ribbon的重试机制
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


# 开启熔断
feign.hystrix.enabled=true
# 开放所有站点(refresh)
management.endpoints.web.exposure.include=*
# 可以远程关闭服务节点
management.endpoint.shutdown.enabled=true
# 可以上报服务的真实健康状态(需要actuator支持)
eureka.client.healthcheck.enabled=true
# 隔离策略，默认是Thread, 可选:线程隔离-Thread｜信号量隔离-Semaphore
hystrix.command.default.execution.isolation.strategy=SEMAPHORE


# 配置中心
# 方式一:直接URL方式查找配置中心
# spring.cloud.config.uri=http://localhost:6900/
# 方式二：通过注册中心查找配置中心
spring.cloud.config.discovery.enabled=true
# 配置中心服务名
spring.cloud.config.discovery.service-id=config-center
# 指定拉取后缀 如 user-consumer-dev.properties
spring.cloud.config.profile=dev
# 拉取分支
spring.cloud.config.label=master

#配置中心的配置
myconfig="Test xxxoo v1"
```

**引入依赖**

```java
	<!-- web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- spring cloud eureka -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <!-- 健康检查需要依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- spring cloud openfeign -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <!-- HttpClient 实现 -->
        <dependency>
            <groupId>io.github.openfeign</groupId>
            <artifactId>feign-httpclient</artifactId>
        </dependency>
        <!-- hystrix 依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        <!-- hystrix-dashboard 依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>
                spring-cloud-starter-netflix-hystrix-dashboard
            </artifactId>
        </dependency>
        <!-- spring cloud config -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>

        <!-- 引入自定义user-api -->
        <dependency>
            <groupId>com.amazecode</groupId>
            <artifactId>user-api</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```

**使用远程配置**

user-consumer-dev.properties

```sh
myconfig="user-consumer-dev"
```

## 刷新配置

### 手动配置热更新

1. 启动类

   ```java
   package com.amazecode.configconsumer;
   
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
   import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
   import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
   import org.springframework.cloud.openfeign.EnableFeignClients;
   
   @SpringBootApplication
   @EnableDiscoveryClient // 可让注册中心发现，适用范围广，不仅限于eureka注册中心
   @EnableFeignClients // 启用Fegin远程调用注解
   @EnableCircuitBreaker // 启用Hystrix注解
   @EnableHystrixDashboard // 启用Hystrix管理页面
   public class ConfigConsumerClientApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(ConfigConsumerClientApplication.class, args);
       }
   
   }
   ```

2. 开启actuator中的refresh端点

3. Controller中添加`@RefreshScope`注解

   ```java
   package com.amazecode.configconsumer.controller;
   
   import com.amazecode.configconsumer.api.ConfigConsumerApi;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.cloud.context.config.annotation.RefreshScope;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RestController;
   
   /**
    * @Author: zhangyadong
    * @Date: 2022/1/7 9:58
    */
   @RestController
   @RefreshScope // 配置变更动态刷新
   public class ConfigConsumerController {
   
       @Autowired
       ConfigConsumerApi configConsumerApi;
   
       /**
        * @description: fegin调用系统内部服务
        * @param:
        * @return: java.lang.String
        * @author: zhangyadong
        * @date: 2022/1/6 8:57
        */
       @GetMapping("/getAlive")
       public String getAlive() {
           System.out.println("config-consumer执行了.................");
           return "config-" + configConsumerApi.isAlive();
       }
   
       /**
        * 从配置中心获取配置
        */
       @Value("${myconfig}")
       String myconfig;
   
       /**
        * 从配置中心获取配置
        */
       @GetMapping("/config")
       public String myConfig() {
           return myconfig;
       }
   }
   ```

4. 先访问下``http://localhost:6901/config`,查看配置文件值，然后修改gitee上user-consumer-dev.properties,的内容，重复访问发现值没有变化

5. 向客户端 url `http://localhost:6901/actuator/refresh`发送Post请求

6. 再次访问``http://localhost:6901/config`，会看到配置值被更新了

### 自动刷新

**erlang安装**

`https://www.erlang.org/downloads`,采用windows安装

**RabbitMQ安装**

`https://www.rabbitmq.com/install-windows.html`,下载windows安装

**配置erlang环境变量**

`ERLANG_HOME=C:\DevelopSoftware\erl-24.2`

path中添加`%ERLANG_HOME%\bin`

cmd验证环境变量是否配置成功

![image-20220107143021891](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\erl-cmd.png)

**配置Rabbitmq环境变量**

`C:\DevelopSoftware\rabbitmq-server-3.9.12\rabbitmq_server-3.9.12\sbin`,这样打开cmd就能运行rabbitmq命令

![image-20220107143631116](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\rabbitmq-plugin-list.png)

cmd开启rabbitmq相关配置

```java
# 启动RabbitMQ节点
rabbitmqctl start_app
# 停止RabbitMQ节点
rabbitmqctl stop   
# 开启RabbitMQ管理模块的插件，并配置到RabbitMQ节点上
rabbitmq-plugins enable rabbitmq_management
```

**管理界面**

http://localhost:15672

用户名密码均为:guest

**服务配置**

**配置文件完整配置**

config-center(application.properties)

```java
# 应用名称
spring.application.name=config-center
server.port=6900
# 配置中心通过git从远程git库更新配置到本地，有时本地拷贝被污染,这时配置中心无法从远程库更新本地配置，设置force-pull=true，可以强制更新
spring.cloud.config.server.git.force-pull=true
# 指定配置地址(这里使用gitee作为git仓库)
spring.cloud.config.server.git.uri=https://gitee.com/xx/config-center.git
# 指定分支
spring.cloud.config.label=master
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/


# 开放所有站点(refresh)
management.endpoints.web.exposure.include=*
# 可以远程关闭服务节点
management.endpoint.shutdown.enabled=true
# 可以上报服务的真实健康状态(需要actuator支持)
eureka.client.healthcheck.enabled=true

# rabbitmq配置
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

```

config-consumer-client(bootstrap.properties)

```java
# 应用名称
eureka.client.service-url.defaultZone=http://localhost:7900/eureka/
server.port=6901
spring.application.name=user-consumer


# fegin默认支持ribbon,Ribbon的重试机制和Fegin重试机制有冲突，所有源码中默认关闭Fegin的重试机制，使用Ribbon的重试机制
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


# 开启熔断
feign.hystrix.enabled=true
# 开放bus-refresh,springcloud 1.5之前配置*即可，2.0之后需要指定配置，否则找不到
management.endpoints.web.exposure.include=*
# 可以远程关闭服务节点
management.endpoint.shutdown.enabled=true
# 可以上报服务的真实健康状态(需要actuator支持)
eureka.client.healthcheck.enabled=true
# 隔离策略，默认是Thread, 可选:线程隔离-Thread｜信号量隔离-Semaphore
hystrix.command.default.execution.isolation.strategy=SEMAPHORE


# 配置中心
# 方式一:直接URL方式查找配置中心
spring.cloud.config.uri=http://localhost:6900/
# 方式二：通过注册中心查找配置中心
#spring.cloud.config.discovery.enabled=true
# 配置中心服务名
#spring.cloud.config.discovery.service-id=config-center
# 指定拉取后缀 如 user-consumer-dev.properties
spring.cloud.config.profile=dev
# 拉取分支
spring.cloud.config.label=master

#配置中心的配置
myconfig="Test xxxoo v1"

# rabbitmq配置
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

**完整依赖**

config-center

```java
<!-- eureka -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <!-- config中心依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <!-- actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- 添加支持mq总线支持 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>
```

config-consumer-client

```java
<!-- web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- spring cloud eureka -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <!-- 健康检查需要依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- spring cloud openfeign -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <!-- HttpClient 实现 -->
        <dependency>
            <groupId>io.github.openfeign</groupId>
            <artifactId>feign-httpclient</artifactId>
        </dependency>
        <!-- hystrix 依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        <!-- hystrix-dashboard 依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
        <!-- spring cloud config -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <!-- 添加支持mq总线支持 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>

        <!-- 引入自定义user-api -->
        <dependency>
            <groupId>com.amazecode</groupId>
            <artifactId>user-api</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```

#### 测试

启动微服务

修改配置文件后，向配置中心发送post请求

http://localhost:6900/actuator/bus-refresh

刷新客户端获取配置的接口看是否刷新了配置

http://localhost:6901/config

**远程配置**

**user-consumer-dev.properties**

```
myconfig="user-consumer-dev"
```

## 自动刷新配置

**完整原理图**

![image-20220108224557419](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\Auto-Refresh-detail.png)

*注意：push配置成功后，配置中心就会去拉取最新配置，并不是在接收到config client拉取配置的请求后配置中心才去git仓库拉取配置的*

**1、配置内网映射**

这里使用的是Natapp，注册Natapp登录网站，申请免费隧道

![image-20220108220951152](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\Natapp-register.png)

配置对于config-center的映射

![image-20220108221219024](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\Natapp-config-in.png)

*注意这里的authtoken，后续启动客户端时会用*

![image-20220108221454227](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\Natapp-config-detail.png)

下载Natapp客户端(win客户端)`otp_win64_24.2.exe`,然后双击打开执行下面命令

```
# b0756359bba97994 即为上边natapp网站内配置隧道时显示的authtoken值
natapp --authtoken=b0756359bba97994
```

![image-20220108222209101](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\Natapp-client.png)

测试是否配置成功，修改user-consumer-dev.properties提交到gitee库后，post请求 http://qedgzk.natappfree.cc/actuator/bus-refresh后，消费端获取到最新配置说明内网映射成功

**2、配置Gitee自动调用刷新接口(webhook)**

首先在config-center项目中，创建一个controller，新增一个refresh接口，用于提供给gitee调用

（原因:经过实践发现，gitee和github的webhook如果直接使用映射地址调用/bus-refresh接口会调用不通,故采用间接方法）

```java
package com.amazecode.configcenter.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: zhangyadong
 * @Date: 2022/1/8 19:45
 */
@RestController
public class RefreshController {

    @PostMapping("/refresh")
    public void refresh(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        // 设置content_type为json要不然会报415的错误
        httpHeaders.add(HttpHeaders.CONTENT_TYPE,"application/json");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null,httpHeaders);
        // 以post方法访问真正的刷新链接
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity("http://localhost:6900/actuator/bus-refresh",
                request, String.class);
    }

}
```

重启config-center、config-consumer-client服务

配置webhook

![image-20220108224151830](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\Gitee-webhook.png)

配置完成后，重新修改user-consumer-dev.properties的属性值，然后直接消费端获取配置查看是否能直接获取最新配置

```
http://localhost:6901/config
```

**spingcloud 项目全家桶暂时写到这里**
