package org.learn.web.service;

import org.learn.web.dao.Dog;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description:
 * @create: 2020-12-02 14:33
 */
@Service
public class ServiceB {

    @Resource
    private ServiceA serviceA;

    public Dog get(int id) {
        return serviceA.get(id);
    }


    public int update(Dog dog) {
        try {
            return serviceA.update(dog);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
