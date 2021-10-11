package com.sth.dubbo.controller;

import com.sth.dubbo.service.DubboRefDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class DubboClientController {

    @Autowired
    private DubboRefDemoService dubboRefDemoService;

    @GetMapping("/s1")
    public @ResponseBody String de(@RequestParam String name) {
        return dubboRefDemoService.sayHello(name);
    }

    @GetMapping("/s2")
    public @ResponseBody String wd(@RequestParam String name) {
        return dubboRefDemoService.gfw(name);
    }


}
