package org.learn.binlogconnector.bean;

import lombok.Data;

import java.util.Date;

@Data
public class BinlogCollectTaskProgress extends CollectTaskProgress {

    private String taskName;

    private String binlogFile;

    private Long position;

    private Date txTime;

}
