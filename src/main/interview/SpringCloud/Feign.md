
Feign 如何指定某台集群调用

1.服务提供方： 注册一个不一样的服务
    A. 配置修改
        application.properties文件中：
            spring.application.name = scm-core-domain-another
》    
    B.代码修改，注册在eureka的服务，提供一个新名字。
        @FeignClient(value = "scm-core-domain-al")
        public interface AddressFacadeService {

2.调用方修改：
    建议不要使用url方式，还没试验通
==>
    @Configuration
    public class LocalFeignConfig {
        @Bean(name = "addressFeignService")
        public AddressFacadeService facadeService(ApplicationContext applicationContext) {
            FeignClientBuilder.Builder<AddressFacadeService> feignClientBuilder = new FeignClientBuilder(applicationContext)
                    .forType(AddressFacadeService.class, "scm-core-domain-al");
            feignClientBuilder.contextId("scm-core-domain-al");
            //feignClientBuilder.url("http://10.180.3.49:8081/");
            AddressFacadeService orgFeignService = feignClientBuilder.build();
            return orgFeignService;
        }
    }
    
===========源码解读：    
spring-cloud-openfeign-core-2.1.1.RELEASE.jar
    org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient
--->
    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        try {
            URI asUri = URI.create(request.url());
            String clientName = asUri.getHost();
            URI uriWithoutHost = cleanUrl(request.url(), clientName);
            FeignLoadBalancer.RibbonRequest ribbonRequest = new FeignLoadBalancer.RibbonRequest(
                    this.delegate, request, uriWithoutHost);
            IClientConfig requestConfig = getClientConfig(options, clientName);
            return lbClient(clientName)
                    .executeWithLoadBalancer(ribbonRequest, requestConfig).toResponse();
        }
        catch (ClientException e) {
            IOException io = findIOException(e);
            if (io != null) {
                throw io;
            }
            throw new RuntimeException(e);
        }
    }

CachingSpringLoadBalancerFactory
    public FeignLoadBalancer create(String clientName) {
		FeignLoadBalancer client = this.cache.get(clientName);
		if (client != null) {
			return client;
		}
		IClientConfig config = this.factory.getClientConfig(clientName);
		ILoadBalancer lb = this.factory.getLoadBalancer(clientName);
		ServerIntrospector serverIntrospector = this.factory.getInstance(clientName,
				ServerIntrospector.class);
		client = this.loadBalancedRetryFactory != null
				? new RetryableFeignLoadBalancer(lb, config, serverIntrospector,
						this.loadBalancedRetryFactory)
				: new FeignLoadBalancer(lb, config, serverIntrospector);
		this.cache.put(clientName, client);
		return client;
	}
	
最终执行：
ribbon-loadbalancer-2.3.0.jar
    LoadBalancerCommand
    AbstractLoadBalancerAwareClient
...    
    public T executeWithLoadBalancer(final S request, final IClientConfig requestConfig) throws ClientException {
        LoadBalancerCommand<T> command = buildLoadBalancerCommand(request, requestConfig);
        try {
            return command.submit(
                new ServerOperation<T>() {
                    @Override
                    public Observable<T> call(Server server) {
                        URI finalUri = reconstructURIWithServer(server, request.getUri());
                        S requestForServer = (S) request.replaceUri(finalUri);
                        try {
                            return Observable.just(AbstractLoadBalancerAwareClient.this.execute(requestForServer, requestConfig));
                        } 
                        catch (Exception e) {
                            return Observable.error(e);
                        }
                    }
                })
                .toBlocking()
                .single();
        } catch (Exception e) {
            Throwable t = e.getCause();
            if (t instanceof ClientException) {
                throw (ClientException) t;
            } else {
                throw new ClientException(e);
            }
        }
    }
        
    