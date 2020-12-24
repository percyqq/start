【背景】
Eureka作为注册中心，主要存在服务发现不及时的问题。
    通过官方描述的客户端与服务器端的通信原理，Eureka Client定时从Eureka Server获取delta信息。
    通过深入研究源码，Eureka Server同时维护了delta信息和Response的缓存。同时，Eureka Server集群同步也有一定的延时。
    这种情况下，一个新服务注册上线后，很可能需要3~4个请求周期才能被整个集群感知。
在容器化部署以后，由于Pod的创建会产生一个全新的IP和hostname，客户端大部分调用都会失败，这个问题显得愈发突出了。

【设计】
要解决上述问题，主要考虑Eureka Client注册成功时，Eureka Server如何及时通知其他客户端。
    考虑采用Long Polling + Epoch的方案，具体方案如下：
1.Eureka Server维护一个整数Epoch，表示当前注册信息的版本号
2.Eureka Client每次请求携带Epoch，并将服务器端返回的Epoch缓存到本地
3.Eureka Server接收到请求时，如果Eureka Client的Epoch等于当前的Epoch，则维持该请求，将请求对象放入内存。
    如果在超时时间之前有Epoch变更，则立即取出维护的请求队列，输出响应结果。如果达到超时时间，则释放请求
4.Eureka Client不断的发起请求，并忽略HTTP超时的错误
为了实现上述方案，需要单独开发Eureka Server/Client SDK。

A》 在Eureka Server，主要是扩展了 InstanceRegistry 类，在注册、取消注册方法时更新Epoch。
    在Long Polling技术上，主要是利用Spring Web的 DeferredResult 来实现。
B》在Eureka Client，主要是扩展了 DiscoveryClient 类。同时需要禁用Eureka Client的默认行为，以避免功能冲突。

【问题】
1.兼容性
    在Eureka Server单独开发了HTTP接口，Epoch也完全独立在原逻辑之外，因此是完全兼容的
    Eureka Client比较特殊，由于Epoch机制和已有的delta机制不完全相同，同时原有的Eureka Server缓存内容比较滞后，
    为了避免数据冲突，需要禁用Eureka Client
2.Epoch无效增长
    Eureka Server集群的同步机制，一定要避免节点自己给自己发送同步消息，否则造成Epoch的无效增长。
    具体方式是将eureka.instance.hostname配置为eureka.client.serviceUrls.defaultZone中的一个节点
3.fail-over机制
    这个方案本身并不能解决服务调用失败的问题。首先Eureka只是AP的系统，注册信息是最终一致的。
    在分布式系统中，通过重试和业务幂等保证服务高可用是常用的手段，也是业务系统自己需要关心的，

【实现原理】
#自动注入
为了对业务完全无感知，minos采用了Spring Boot的auto-configure特性，将核心的bean注册到依赖注入容器中，并完成对核心bean的替换或覆盖。
在大多数情况下，应用是无需任何改动代码的。但是涉及到多线程场景，需要进行改造，可以参考下面”灰度标机传递“的描述。

#Eureka及时感知
在使用Eureka作为注册中心时，客户端与服务器端每30s（这个时间可配置）进行一次服务同步，这样导致服务的下线对依赖方不是及时感知的。
    minos主要利用long-polling技术解决这个问题。Long-polling是一个常用的服务器端推送技术。
    客户端发起一个HTTP请求，服务器端判断该请求的资源是否有变更，如果有变更，则立即返回结果；
    如果没有变更，则挂起请求，直到变更来临时立即返回结果；如果挂起时间足够长，连接会超时断开，客户端立即重新发起请求。
    在服务注册发现的场景下，我们可以维护一个版本号，当有注册信息变更则提升版本号，客户端将版本号作为订阅条件。
首先，我们对Eureka Server进行了改造，支持了版本变更机制和long-polling。
    为了提升性能，Eureka Server还维护了变更事件列表，在返回变更消息时，只返回2个版本号之间的具体事件，这样大大减少了网络传输数据量。
    minos-discovery包扩展了Eureka-Client的相关行为，利用long-polling技术和Eureka Server进行交互。
    同时，minos-discovery还可以发起服务变更事件，方便应用进行自定义的处理。
...经过测试，服务上下线感知时间从原来的>30s缩短到了<1s。

#灰度标记传递
minos-spring包扩展了Spring Mvc、Spring RestTemplate、Spring Cloud OpenFeign等框架的行为，以支持灰度标记传递。
首先，minos定义了一个Filter，用于在请求到达时解析请求头里面的灰度标记，并存储到ThreadLocal中供后续使用。
minos针对RestTemplate实现了自己的ClientHttpRequestInterceptor，针对FeignClient实现了自己的RequestInterceptor，
`这样在发起对其他服务的调用时，以业务无感知的方式从ThreadLocal取出灰度标记，放到请求头里。

#!在多线程场景下，标记传递会比较复杂。
   如果是自定义线程池，ThreadLocal消息会丢失，此时需要用minos包里 Executors提供的方法来改造代码，这个辅助类会创造一个特殊的线程池。
   如果是用到Spring中的@Async注解，minos会自动拦截关键的方法，利用上述特殊的线程池替换默认的线程池。
   这个特殊的线程池逻辑也不复杂，主要是在execute方法执行时，从当前线程上下文中复制所有信息，并传入一个包装类Runnable / Callable。
   这个Runnable / Callable会在执行时将前面传递的信息直接复制到线程上下文。这样也就实现了线程上下文的跨线程传递。
alibaba ==> transmittable-thread-local-2.11.0.jar

#负载均衡
minos-loadbalance包扩展了Spring Cloud Ribbon，以支持基于灰度发布的负载均衡。
    在上述灰度标记传递的机制下，minos注入了自定义的MinosLoadBalanceRule类。
    在负载均衡时，如果存在灰度标记，则优先选择灰度节点，当灰度节点不存在时，降级选择稳定节点。