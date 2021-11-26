package org.learn.binlogconnector.bean;

import lombok.Data;

import java.util.Date;

@Data
public class CollectTaskProgress {

    protected long id;

    protected String name;

    protected int type;

    protected String meta;

    protected String progress;

    protected Date createTime;

    protected Date updateTime;


}
