package com.sth.dubbo.controller;

import com.sth.dubbo.WtfService;
import com.sth.dubbo.DemoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class DubboClientController {

    @DubboReference(validation = "1.0.0")
    private DemoService demoService;


    @DubboReference(validation = "1.0.0")
    private WtfService wtfService;

    @GetMapping("/test")
    public String de(@RequestParam String name) {
        String ret = wtfService.gfw(name);

        System.out.println(" =====>>>> " + ret);

        return demoService.sayHello(name);
    }


}
