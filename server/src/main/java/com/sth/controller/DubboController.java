package com.sth.controller;


import com.sth.dubbo.DemoService;
import com.sth.dubbo.WtfService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DubboController implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    private WtfService wtfService;

    @GetMapping("/test")
    public String wtf(@RequestParam String name) {
        wtfService.gfw(name);
        DemoService demoService = applicationContext.getBean(DemoService.class);
        return demoService.sayHello(name);
    }



}


