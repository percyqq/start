package com.sth.dubbo.service;

import com.sth.dubbo.DemoService;
import com.sth.dubbo.WtfService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class DubboRefDemoService implements DemoService, WtfService {

    @DubboReference(version = "1.0.0")
    private WtfService wtfService;

    @DubboReference(version = "1.0.0")
    private DemoService demoService;

    @Override
    public String sayHello(String name) {
        return demoService.sayHello(name);
    }

    @Override
    public String gfw(String sth) {
        return wtfService.gfw(sth);
    }


}
