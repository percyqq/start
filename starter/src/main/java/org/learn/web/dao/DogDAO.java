/*
 */
package org.learn.web.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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