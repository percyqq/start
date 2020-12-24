package org.learn.web;

import org.learn.web.dao.Dog;
import org.learn.web.service.ServiceA;
import org.learn.web.service.ServiceB;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @create: 2020-07-24 16:34
 */
@RestController
public class AController {

    @Resource
    private ServiceB serviceB;

    private static Random random = new Random();


    private static int id = 0;


    @RequestMapping("/dogs")
    public List<Dog> a11() {
        int time = 1 + random.nextInt(5);
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Dog> dogs = new ArrayList<>();
        for (int i = 0; i < time; i++) {
            Dog dog = new Dog(id, "god" + id, "很长的string 就对了、 请得起 哪块到 √ code is _(:з」∠)_很屌！<>d",
                    "整个非长的str， 再说。。。总之开水奇偶杰尔马是空瑟吉欧就是节目出激昂", new Date());
            id++;
            dogs.add(dog);
        }
        return dogs;
    }

    @RequestMapping("/ping")
    public String a1(@RequestParam("id") int id) {
        int time = random.nextInt(5);
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success, sleep : " + time + ", id : " + id;
    }


    @RequestMapping("/st")
    public String a(@RequestParam("id") int id) {
        Dog dog = serviceB.get(id);
        dog.setName("135");
        serviceB.update(dog);
        return "wtf.";
    }
}
