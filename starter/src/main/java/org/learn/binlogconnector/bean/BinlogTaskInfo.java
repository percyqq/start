package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class BinlogTaskInfo {

    private String uniqueTask;

    private TaskStatus status;

    private TaskStartPositionStatusEnum taskStartPositionStatus;

    private String msg;

    private Map<String, Set<String>> dbTableMap;

    private BinlogPosition currentPosition;

    public BinlogTaskInfo(String msg) {
        this.msg = msg;
    }

}
