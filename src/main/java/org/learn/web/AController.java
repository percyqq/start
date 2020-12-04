package org.learn.web;

import org.learn.web.dao.Dog;
import org.learn.web.service.ServiceA;
import org.learn.web.service.ServiceB;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @create: 2020-07-24 16:34
 */
@RestController
public class AController {

    @Resource
    private ServiceB serviceB;

    @RequestMapping("/st")
    public String a(@RequestParam("id") int id) {
        Dog dog = serviceB.get(id);
        dog.setName("135");
        serviceB.update(dog);
        return "wtf.";
    }
}
