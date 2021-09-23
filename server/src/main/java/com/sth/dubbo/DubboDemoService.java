package com.sth.dubbo;


import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("demoService") // 用于dubbo:server 的ref 参数
@DubboService(version = "1.0.0", interfaceClass = DemoService.class,
        //registry = "registry1",
        delay = 5000)

public class DubboDemoService implements DemoService {

    /**
     * The default value of ${dubbo.application.name} is ${spring.application.name}
     */
    @Value("${dubbo.application.name}")
    private String serviceName;

    @Override
    public String sayHello(String name) {
        return String.format("[%s] : Hello, %s", serviceName, name);
    }

}
