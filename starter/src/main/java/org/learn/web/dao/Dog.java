/*
 * Copyright (C) 2012-2025 shishike Technology(Beijing) Chengdu Co. Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * shishike Technology(Beijing) Chengdu. You shall can disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements you entered into with
 * shishike Technology(Beijing) Chengdu.
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