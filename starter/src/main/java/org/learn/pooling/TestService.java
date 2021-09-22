package org.learn.pooling;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.learn.web.dao.Dog;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @create: 2020-12-24 14:11
 */
@Service
@Slf4j
public class TestService {

    DishShopDao forFun;
    private static RestTemplate restTemplate;
    private static ExecutorService executorService;

    private static final Integer MAX_TOTAL = 30;              //连接池最大连接数
    private static final Integer MAX_PER_ROUTE = 10;          //单个路由默认最大连接数
    private static final Integer REQ_TIMEOUT = 5 * 1000;      //请求超时时间ms
    private static final Integer CONN_TIMEOUT = 5 * 1000;     //连接超时时间ms
    private static final Integer SOCK_TIMEOUT = 10 * 1000;    //读取超时时间ms

    static {
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(REQ_TIMEOUT)
                .setConnectTimeout(CONN_TIMEOUT).setSocketTimeout(SOCK_TIMEOUT)
                .build();

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(MAX_TOTAL);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, false))
                .setConnectionManager(poolingHttpClientConnectionManager)
                .build();

        //HttpClientFactory.thread=new HttpClientConnectionMonitorThread(poolingHttpClientConnectionManager); //管理 http连接池
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);


        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objectMapper.setDateFormat(dateFormat);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(simpleModule);

        restTemplate = new RestTemplate(requestFactory);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));

        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);

        restTemplate.setMessageConverters(converters);

        executorService = new ThreadPoolExecutor(12, 12, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(true),
                new ScmThreadFactory("dish-sync-mq"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static class ScmThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public ScmThreadFactory(String threadName) {
            namePrefix = "pool-" + threadName + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            t.setUncaughtExceptionHandler((thread, throwable) -> log.error(throwable.getMessage(), throwable));
            return t;
        }
    }

    public static void main(String[] args) {
        new TestService().update();
    }

    public void update() {
        try {
            TimeUnit.SECONDS.sleep(11);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("###################################################### start.. ######################################################");

        try {
            for (int i = 0; i < 1; i++) {
                int finalI = i;
                executorService.execute(new LeakThread(finalI, forFun));
            }
        } catch (RejectedExecutionException ree) {
            ree.printStackTrace();
        }


    }

    private static class DishShopDao{}

    private static class LeakThread implements Runnable {

        private int index;
        private DishShopDao dishShopDao;

        private LeakThread(int index, DishShopDao dishShopDao) {
            this.index = index;
            this.dishShopDao = dishShopDao;
        }

        @Override
        public final void run() {
            try {
                log.info(", start.. LOOP: " + index);
                //RequestContext.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));

                String url = "http://127.0.0.1:3603/start/dogs?id=" + index;

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                HttpEntity entity = new HttpEntity(headers);
                Map<String, String> params = new HashMap<>();
                params.put("merid", "merid");

                ParameterizedTypeReference<List<Dog>> responseType = new ParameterizedTypeReference<List<Dog>>() {
                };
                ResponseEntity<List<Dog>> resp = restTemplate.exchange(url, HttpMethod.GET, entity, responseType, params);

                List<Dog> data = resp.getBody();
                log.info(Thread.currentThread().getName() + ", ret : " + data.size() + ", data: " + data);


//                List<DishShop> dishShops = data.stream().map(dog -> {
//                    DishShop dishShop = new DishShop();
//                    dishShop.setId(dog.getId().longValue() * -1);
//                    dishShop.setServerUpdateTime(dog.getTime());
//                    return dishShop;
//                }).collect(Collectors.toList());
//                for (DishShop dishShop : dishShops) {
//                    dishShop.setUuid(null);
//                    dishShopDao.updateSelective(dishShop);
//                }

            } catch (Exception e) {
                if (e != null) {
                    log.error("Receive Message 【{}】 >> {}  Error >>", e.getMessage(), e);
                }
            } finally {
                //RequestContext.remove();
            }
        }
    }

}

