package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * binlog查询的 entity
 * */
public class BinlogPos {
    private String logFile;
    private long position;
    private String eventType;

    public static final String GTID = "gtid";
}