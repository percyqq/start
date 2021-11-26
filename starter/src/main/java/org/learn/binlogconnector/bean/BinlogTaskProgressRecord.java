package org.learn.binlogconnector.bean;

import lombok.Data;

import java.util.Date;

@Data
public class BinlogTaskProgressRecord {

    private int id;

    private String taskName;

    private String binlogFile;

    private Long position;

    private Date txTime;

    private Date createTime;


}
