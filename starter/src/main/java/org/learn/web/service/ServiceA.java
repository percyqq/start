package org.learn.web.service;

import org.learn.web.dao.Dog;
import org.learn.web.dao.DogDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @description:
 * @create: 2020-12-02 11:11
 */
@Service
public class ServiceA {

    @Resource
    private DogDAO dogDAO;


    public Dog get(int id) {
        return dogDAO.selectByPrimaryKey(id);
    }

    @Transactional
    public int update(Dog dog) throws Exception {
        int update = dogDAO.updateByPrimaryKeySelective(dog);
        if (update == 1) {
            throw new Exception("update fail ");
        }
        return update;
    }
}
