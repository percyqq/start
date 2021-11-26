/*

 */
package org.learn.web.dao;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * dog
 */
@Data
@AllArgsConstructor
public class Dog implements Serializable {

    private Integer id;

    private String name;

    private String code;

    private String test;

    private Date time;

}