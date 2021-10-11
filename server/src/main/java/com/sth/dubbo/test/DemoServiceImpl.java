package com.sth.dubbo.test;

import com.sth.dubbo.DemoService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@DubboService(version = "1.0.0", interfaceClass = DemoService.class,
        //registry = "registry",
        delay = 5000, timeout = 15000)
public class DemoServiceImpl implements DemoService {

    @Value("${dubbo.application.name}")
    private String serviceName;

    @Override
    public String sayHello(String name) {
        return String.format("[%s] : DubboService : Hello, %s", serviceName, name);
    }


}
