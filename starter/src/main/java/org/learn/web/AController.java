package org.learn.web;

import lombok.extern.slf4j.Slf4j;
import org.learn.web.dao.DishShop;
import org.learn.web.dao.Dog;
import org.learn.web.service.ServiceB;
import org.springframework.web.bind.annotation.GetMapping;
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
@Slf4j
public class AController {

    @Resource
    private ServiceB serviceB;


    private static int id = 0;
    private static Random random = new Random();

    @RequestMapping("/dogs")
    public List<DishShop> a11() {
        int time = 2 + random.nextInt(5);
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<DishShop> dogs = new ArrayList<>();
        for (int i = 0; i < time; i++) {
            Dog dog = new Dog(id, "god" + id, "很长的string 就对了、 请得起 哪块到 √ code is _(:з」∠)_很屌！<>d",
                    "整个非长的str， 再说。。。总之开水奇偶杰尔马是空瑟吉欧就是节目出激昂", new Date());
            id++;

            DishShop ds = new DishShop();
            ds.setId(i == 0 ? 145955L : 145892L);
            ds.buildData(i);
            dogs.add(ds);
        }
        log.info(Thread.currentThread().getName() + ", request");
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
