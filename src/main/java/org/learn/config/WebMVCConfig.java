package org.learn.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration
@EnableWebMvc
@Slf4j
public class WebMVCConfig implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(final DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        //定义Json转换器
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        //定义对象映射器
        ObjectMapper objectMapper = new ObjectMapper();
        //定义对象模型
        SimpleModule simpleModule = new SimpleModule();
        //添加对长整型的转换关系
        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        simpleModule.addSerializer(Date.class, new JsonSerializer<Date>() {
            @Override
            public void serialize(Date value, JsonGenerator jsonGenerator, SerializerProvider provider)
                    throws IOException {
                SimpleDateFormat dateTimeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = dateTimeFmt.format(value);
                jsonGenerator.writeString(dateString);
            }
        });
        //将对象模型添加至对象映射器
        objectMapper.registerModule(simpleModule);
        //将对象映射器添加至Json转换器
        jackson2HttpMessageConverter.setObjectMapper(objectMapper);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return jackson2HttpMessageConverter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 文件下载损坏
        converters.add(new ByteArrayHttpMessageConverter());

        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = jackson2HttpMessageConverter();

        //在转换器列表中添加自定义的Json转换器
        converters.add(jackson2HttpMessageConverter);
        //添加utf-8的默认String转换器
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

}
