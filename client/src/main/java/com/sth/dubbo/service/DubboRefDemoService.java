package com.sth.dubbo.service;

import com.sth.dubbo.DemoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class DubboRefDemoService implements DemoService {

    @DubboReference
    private DemoService demoService;

    @Override
    public String sayHello(String name) {
        return demoService.sayHello(name);
    }

}
