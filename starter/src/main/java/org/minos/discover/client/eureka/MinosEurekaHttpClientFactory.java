package org.minos.discover.client.eureka;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.converters.jackson.mixin.ApplicationsJsonMixIn;
import com.netflix.discovery.converters.jackson.mixin.InstanceInfoJsonMixIn;
import com.netflix.discovery.converters.jackson.serializer.InstanceInfoJsonBeanSerializer;
import com.netflix.discovery.shared.Applications;
import com.netflix.discovery.shared.resolver.EurekaEndpoint;
import org.minos.discover.client.MinosServiceInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

public class MinosEurekaHttpClientFactory {

    private static BeanSerializerModifier createJsonSerializerModifier() {
        return new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                                      BeanDescription beanDesc, JsonSerializer<?> serializer) {
                if (beanDesc.getBeanClass().isAssignableFrom(InstanceInfo.class)) {
                    return new InstanceInfoJsonBeanSerializer((BeanSerializerBase) serializer, false);
                }
                return serializer;
            }
        };
    }

    public MinosEurekaHttpClient newClient(MinosServiceInstance instance, EurekaEndpoint endpoint, int timeout) {
        return new DefaultMinosEurekaHttpClient(restTemplate(endpoint.getServiceUrl()), instance, endpoint, timeout);
    }

    public MinosEurekaHttpClient newClient(EurekaEndpoint endpoint, int timeout) {
        return new DefaultMinosEurekaHttpClient(restTemplate(endpoint.getServiceUrl()), endpoint, timeout);
    }

    private RestTemplate restTemplate(String serviceUrl) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            URI serviceUri = new URI(serviceUrl);
            if (serviceUri.getUserInfo() != null) {
                String[] credentials = serviceUri.getUserInfo().split(":");
                if (credentials.length == 2) {
                    restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(
                            credentials[0], credentials[1]));
                }
            }
        } catch (URISyntaxException ignore) {

        }

        restTemplate.getMessageConverters().add(0, mappingJacksonHttpMessageConverter());
        restTemplate.setErrorHandler(new ErrorHanlder());

        return restTemplate;
    }

    public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE));

        SimpleModule jsonModule = new SimpleModule();
        jsonModule.setSerializerModifier(createJsonSerializerModifier());
        converter.getObjectMapper().registerModule(jsonModule);

        converter.getObjectMapper().configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        converter.getObjectMapper().configure(DeserializationFeature.UNWRAP_ROOT_VALUE,
                true);
        converter.getObjectMapper().addMixIn(Applications.class, ApplicationsJsonMixIn.class);
        converter.getObjectMapper().addMixIn(InstanceInfo.class, InstanceInfoJsonMixIn.class);

        return converter;
    }

    class ErrorHanlder extends DefaultResponseErrorHandler {
        @Override
        protected boolean hasError(HttpStatus statusCode) {
            if (statusCode.is4xxClientError()) {
                return false;
            }
            return super.hasError(statusCode);
        }
    }

}
