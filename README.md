# springcloud全家桶
## 服务注册与发现

#### 注册中心和微服务间的关系

> ![image-20220103210636654](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\eureka-server-client.png)   ------------------------------《服务注册与发现关系图》

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

![image-20220103211443787](D:\Work\LearnSpace\SelfGitWorkSpace\AmazeCode\springcloud\img\eureka-server-dashboard.png)