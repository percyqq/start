package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CollectorTaskType {

    //binlog 的 type 在数据库中存的type 都是2，都是按同一类存储。
    BINLOG_REALTIME(2, "BINLOG", "RealTimeBinlogTask", ""),
    BINLOG_SPECIFIED(2, "BINLOG", "SpecifiedPositionBinlogTask", ""),
    TABLE_FULL(3, "SQL", "TableFullSyncTask", "单表全量SQL任务"),

    TABLE_QUERY(10, "SQL-QUERY", "TableQueryTask", "SQL查询任务"),
    BINLOG_QUERY(11, "BINLOG-QUERY", "BinlogQueryTask", "BINLOG查询任务"),
    ;

    private int type;
    private String typeName;
    private String unique;
    private String desc;

}
