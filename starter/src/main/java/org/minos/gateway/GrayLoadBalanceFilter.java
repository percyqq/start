//package org.minos.gateway;
//
//import org.minos.core.consts.ContextConstraints;
//import org.minos.core.consts.RequestConstraints;
//import org.minos.core.context.ContextHolder;
//import org.minos.loadbalance.NoAvailableServersException;
//import org.reactivestreams.Publisher;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.client.ServiceInstance;
//import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
//import org.springframework.core.Ordered;
//import org.springframework.core.io.buffer.DataBuffer;
//import org.springframework.core.io.buffer.DataBufferFactory;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ReactiveHttpOutputMessage;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.StringUtils;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.net.URI;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Supplier;
//
//import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
//import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR;
//
///**
// * GrayLoadBalanceFilter
// *
// * @date 2020/06/03
// */
//public class GrayLoadBalanceFilter implements GlobalFilter, Ordered {
//
//    private static final int FILTER_ORDER = LoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER - 10;
//
//    private static final String EMPTY_VERSION = "";
//
//    private ContextHolder contextHolder;
//
//    private LoadBalancerClient loadBalancer;
//
//    private static Logger logger = LoggerFactory.getLogger(GrayLoadBalanceFilter.class);
//
//    public GrayLoadBalanceFilter(
//            ContextHolder contextHolder,
//            LoadBalancerClient loadBalancer) {
//        this.contextHolder = contextHolder;
//        this.loadBalancer = loadBalancer;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
//        String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);
//
//        logger.debug("request info {} {} {}", exchange.getRequest().getURI(), exchange.getRequest().getMethod(), exchange.getRequest().getHeaders());
//
//        if (url == null || (!"lb".equals(url.getScheme()) && !"lb".equals(schemePrefix))) {
//            logger.debug("This is not a load balance request, skipped");
//            return chain.filter(exchange);
//        }
//
//        return getVersion(exchange).flatMap(version -> {
//            contextHolder.put(ContextConstraints.KRY_LABEL_VERSION, version);
//            final ServiceInstance instance = loadBalancer.choose(url.getHost());
//            contextHolder.clear();
//
//            if (instance == null) {
//                throw new NoAvailableServersException("Unable to find instance for " + url.getHost());
//            }
//
//            URI requestUri = exchange.getRequest().getURI();
//
//            // if the `lb:<scheme>` mechanism was used, use `<scheme>` as the default,
//            // if the loadbalancer doesn't provide one.
//            String overrideScheme = null;
//            if (schemePrefix != null) {
//                overrideScheme = url.getScheme();
//            }
//
//            URI requestUrl = loadBalancer.reconstructURI(new DelegatingServiceInstance(instance, overrideScheme), requestUri);
//            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
//            logger.debug("MinosGatewayFilter url chosen: " + requestUrl);
//
//            return chain.filter(exchange);
//        });
//    }
//
//    private Mono<String> getVersion(ServerWebExchange exchange) {
//        HttpHeaders headers = exchange.getRequest().getHeaders();
//
//        //如果header本身带了version，走泳道
//        if (headers.containsKey(RequestConstraints.KRY_LABEL_VERSION)) {
//            List<String> versions = headers.get(RequestConstraints.KRY_LABEL_VERSION);
//            if (!CollectionUtils.isEmpty(versions)) {
//                for (String version : versions) {
//                    if (!StringUtils.isEmpty(version)) {
//                        return Mono.just(version);
//                    }
//                }
//            }
//        }
//
//        return Mono.just(EMPTY_VERSION);
//    }
//
//    @Override
//    public int getOrder() {
//        return FILTER_ORDER;
//    }
//
//
//    class DelegatingServiceInstance implements ServiceInstance {
//        final ServiceInstance delegate;
//        private String overrideScheme;
//
//        DelegatingServiceInstance(ServiceInstance delegate, String overrideScheme) {
//            this.delegate = delegate;
//            this.overrideScheme = overrideScheme;
//        }
//
//        @Override
//        public String getServiceId() {
//            return delegate.getServiceId();
//        }
//
//        @Override
//        public String getHost() {
//            return delegate.getHost();
//        }
//
//        @Override
//        public int getPort() {
//            return delegate.getPort();
//        }
//
//        @Override
//        public boolean isSecure() {
//            return delegate.isSecure();
//        }
//
//        @Override
//        public URI getUri() {
//            return delegate.getUri();
//        }
//
//        @Override
//        public Map<String, String> getMetadata() {
//            return delegate.getMetadata();
//        }
//
//        @Override
//        public String getScheme() {
//            String scheme = delegate.getScheme();
//            if (scheme != null) {
//                return scheme;
//            }
//            return this.overrideScheme;
//        }
//
//    }
//
//    class CachedBodyOutputMessage implements ReactiveHttpOutputMessage {
//
//        private final DataBufferFactory bufferFactory;
//
//        private final HttpHeaders httpHeaders;
//
//        private Flux<DataBuffer> body = Flux.error(new IllegalStateException(
//                "The body is not set. " + "Did handling complete with success?"));
//
//        CachedBodyOutputMessage(ServerWebExchange exchange, HttpHeaders httpHeaders) {
//            this.bufferFactory = exchange.getResponse().bufferFactory();
//            this.httpHeaders = httpHeaders;
//        }
//
//        @Override
//        public void beforeCommit(Supplier<? extends Mono<Void>> action) {
//
//        }
//
//        @Override
//        public boolean isCommitted() {
//            return false;
//        }
//
//        @Override
//        public HttpHeaders getHeaders() {
//            return this.httpHeaders;
//        }
//
//        @Override
//        public DataBufferFactory bufferFactory() {
//            return this.bufferFactory;
//        }
//
//        /**
//         * Return the request body, or an error stream if the body was never set or when.
//         *
//         * @return body as {@link Flux}
//         */
//        public Flux<DataBuffer> getBody() {
//            return this.body;
//        }
//
//        @Override
//        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
//            this.body = Flux.from(body);
//            return Mono.empty();
//        }
//
//        @Override
//        public Mono<Void> writeAndFlushWith(
//                Publisher<? extends Publisher<? extends DataBuffer>> body) {
//            return writeWith(Flux.from(body).flatMap(p -> p));
//        }
//
//        @Override
//        public Mono<Void> setComplete() {
//            return writeWith(Flux.empty());
//        }
//
//    }
//}
