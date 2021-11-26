package org.learn.binlogconnector;


import com.github.shyiko.mysql.binlog.event.Event;
import org.learn.binlogconnector.bean.TableData;

public interface BinlogEventParseCallback {

    boolean shouldSkip(String database, String tableName);

    /**
     * @param binlogFilename 新创建的binlog文件名字
     * @param binlogPosition 新创建的binlog起点位置
     */
    void onReadingNewBinlogFileOrNewConnectionEstablished(long txTime, String binlogFilename, long binlogPosition);

    /**
     * @param nextBinlogPosition 是取的下一个事务的开始位置 1个事务完成时才结束，设置为下一个事务，作为续传的起点
     */
    void onEndOfTransaction(long txTime, long currentPosition, long nextBinlogPosition);

    /**
     * 每次事务开始都会执行此事件查询当前操作的表
     */
    void onTableMapEvent(TableData currentTable);

    /**
     * 当 辅助库执行了 目标库对应的DDL语句之后
     */
    void onDDL(long txTime, long nextPosition, String database, String tableName, String originalSql);

    /**
     * 当 辅助库执行了 目标库对应的DDL语句之后
     */
    void onDML(long txTime, long nextPosition, TableData currentTable, Object data, String eventType, boolean isUpdate, boolean isInsert);

    void onEventParseException(Event event, Exception e);
}
