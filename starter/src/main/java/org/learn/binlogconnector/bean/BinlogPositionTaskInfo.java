package org.learn.binlogconnector.bean;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
/**
 * 任务进度记录
 * */
public class BinlogPositionTaskInfo extends BinlogPosition {

    private final boolean realTime;

    private String taskTypeName;

    private String uniqueTask;

    /**
     * 事务发生的历史时间
     * */
    private long txTime;

    public BinlogPositionTaskInfo(String fileName, long position, boolean realTime, String taskTypeName, String uniqueTask) {
        super(fileName, position);
        this.realTime = realTime;
        this.taskTypeName = taskTypeName;
        this.uniqueTask = uniqueTask;
    }
}
