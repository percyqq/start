

5.Hystrix通过为每个依赖服务分配独立的线程池进行资源隔离，从而避免整个服务雪崩。这样做带来的代价就是维护多个线程池需要额外的性能开销。
雪崩效应常见场景
硬件故障：如服务器宕机，机房断电，光纤被挖断等。
流量激增：如异常流量，重试加大流量等。
缓存穿透：一般发生在应用重启，所有缓存失效时，以及短时间内大量缓存失效时。大量的缓存不命中，使请求直击后端服务，造成服务提供者超负荷运行，引起服务不可用。
程序BUG：如程序逻辑导致内存泄漏，JVM长时间FullGC等。
同步等待：服务间采用同步调用模式，同步等待造成的资源耗尽。


线程隔离-线程池
Hystrix通过命令模式对发送请求的对象和执行请求的对象进行解耦，将不同类型的业务请求封装为对应的命令请求。如订单服务查询商品，查询商品请求->商品Command；商品服务查询库存，查询库存请求->库存Command。并且为每个类型的Command配置一个线程池，当第一次创建Command时，根据配置创建一个线程池，并放入ConcurrentHashMap，

通常情况下，线程池引入的开销足够小，不会有重大的成本或性能影响。但对于一些访问延迟极低的服务，如只依赖内存缓存，线程池引入的开销就比较明显了，这时候使用线程池隔离技术就不适合了，
我们需要考虑更轻量级的方式，如信号量隔离。

public QueryByOrderIdCommandSemaphore(OrderServiceProvider orderServiceProvider) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("orderService"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("queryByOrderId"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withCircuitBreakerRequestVolumeThreshold(10)////至少有10个请求，熔断器才进行错误率的计算
                        .withCircuitBreakerSleepWindowInMilliseconds(5000)//熔断器中断请求5秒后会进入半打开状态,放部分流量过去重试
                        .withCircuitBreakerErrorThresholdPercentage(50)//错误率达到50开启熔断保护
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(10)));//最大并发请求量
        this.orderServiceProvider = orderServiceProvider;
    }


Hystrix如何实现这些设计目标？
使用命令模式将所有对外部服务（或依赖关系）的调用包装在HystrixCommand或HystrixObservableCommand对象中，并将该对象放在单独的线程中执行；
每个依赖都维护着一个线程池（或信号量），线程池被耗尽则拒绝请求（而不是让请求排队）。
记录请求成功，失败，超时和线程拒绝。
服务错误百分比超过了阈值，熔断器开关自动打开，一段时间内停止对该服务的所有请求。
请求失败，被拒绝，超时或熔断时执行降级逻辑。
近实时地监控指标和配置的修改。
