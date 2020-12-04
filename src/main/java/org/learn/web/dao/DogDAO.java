/*
 * Copyright (C) 2012-2025 shishike Technology(Beijing) Chengdu Co. Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * shishike Technology(Beijing) Chengdu. You shall can disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements you entered into with
 * shishike Technology(Beijing) Chengdu.
 */
package org.learn.web.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.learn.web.dao.Dog;
import org.learn.web.dao.DogExample;

@Mapper
public interface DogDAO {
    long countByExample(DogExample example);

    int deleteByExample(DogExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Dog record);

    int insertSelective(Dog record);

    List<Dog> selectByExample(DogExample example);

    Dog selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Dog record, @Param("example") DogExample example);

    int updateByExample(@Param("record") Dog record, @Param("example") DogExample example);

    int updateByPrimaryKeySelective(Dog record);

    int updateByPrimaryKey(Dog record);
}