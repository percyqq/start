package sth.controller;


import com.sth.dubbo.DemoService;
import org.springframework.beans.BeansException;
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


    @GetMapping("/test")
    public String wtf(@RequestParam String name) {
        DemoService demoService = applicationContext.getBean(DemoService.class);
        return demoService.sayHello(name);
    }



}


