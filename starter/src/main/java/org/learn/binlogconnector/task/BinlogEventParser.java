package org.learn.binlogconnector.task;

import com.github.shyiko.mysql.binlog.event.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.learn.binlogconnector.BinlogEventParseCallback;
import org.learn.binlogconnector.bean.BinlogPositionTaskInfo;
import org.learn.binlogconnector.bean.TableData;
import org.learn.binlogconnector.util.TableDDLMatcherUtil;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

@Slf4j
public class BinlogEventParser {

    private BinlogPositionTaskInfo taskInfo;
    private final BinlogEventParseCallback eventParseCallback;

    /***
     * 当遇到TABLE_MAP时，不是监听集合中的数据，skipLoop = true
     * 那么至XID结束的所有事件都不会监听
     */
    private boolean skipLoop = false;

    /**
     * Client监听的是整个Server的，不区分schema。 一个事务的结构如下，当遇到TABLE_MAP时，讲当前正在处理的table[ schema + table ]记录下来 事件虽然会返回tableId，但是存在不同schema的tableId一样。
     * <1>
     * GTID __ xxx __ TABLE_MAP XID
     * </1>
     *
     * <2>
     * GTID QUERY 执行DDL，目前有这2种流程场景，
     * </2>
     */
    private TableData currentTable = null;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BinlogEventParser(BinlogPositionTaskInfo taskInfo, BinlogEventParseCallback eventParseCallback) {
        this.taskInfo = taskInfo;
        this.eventParseCallback = eventParseCallback;
    }

    public void parseEvent(Event event) {
        try {
            parseEvent0(event);
        } catch (Exception e) {
            eventParseCallback.onEventParseException(event, e);
        }
    }

    private void parseEvent0(Event event) {
        EventData eventData = event.getData();
        EventHeader header = event.getHeader();

        boolean canFetchData = false, isUpdate = false, isInsert = false, isDelete = false;
        // 事务发生的时间
        long txTime = header.getTimestamp();

        // XID.getNextPosition === GTID.getPosition
        EventType eventType = header.getEventType();
        if (eventType == EventType.GTID || eventType == EventType.ANONYMOUS_GTID) {
            //一个事务的开始   eventData === null
        } else if (eventType == EventType.QUERY) {
            // query事件有 事务开始的begin 和 DDL事件组成，DDL事件没有没有 tableId
            QueryEventData data = (QueryEventData) eventData;
            EventHeaderV4 headerV4 = (EventHeaderV4) header;

            String originalSql = data.getSql();
            //事务开始
            if ("begin".equalsIgnoreCase(originalSql)) {
                return;
            }

            // CREATE DATABASE IF NOT EXISTS `slow_query_log` /* pt-query-digest */
            String tableName = TableDDLMatcherUtil.matchTableName(originalSql);
            if (tableName == null) {
                return;
            }
            //DDL 的一些语句是拿不到database的！(如果连接数据库的时候不指定 database！) data.getDatabase() ==> ""
            String database = data.getDatabase();
            if (StringUtils.isEmpty(database) && currentTable == null) {
                // 如果连接数据库的时候不指定 database。。
                database = TableDDLMatcherUtil.matchDatabaseName(originalSql);
                if (log.isDebugEnabled()) {
                    log.debug("{} task[{}] connection has not specified database! try match database, result : {}, sql : {}",
                        taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(), database, originalSql);
                }
            } else if (StringUtils.isEmpty(database) && currentTable != null) {
                database = currentTable.getDatabase();
            }

            if (StringUtils.isEmpty(database)) {
                //下面的 shouldSkip 由于为 null / ""，返回true..
                log.warn("{} task[{}] match database failed! sql : {}", taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(), originalSql);
                return;
            }

            if (eventParseCallback.shouldSkip(database, tableName)) {
                skipLoop = true;
                return;
            }

            // 正常的一轮循环只有一次QUERY， 但是DDL执行完成只有
            skipLoop = false;
            eventParseCallback.onDDL(txTime, headerV4.getNextPosition(), database, tableName, originalSql);

            currentTable = null;
            return;
        } else if (eventType == EventType.TABLE_MAP) {
            TableMapEventData tableMapEventData = (TableMapEventData) eventData;
            String database = tableMapEventData.getDatabase();
            String tableName = tableMapEventData.getTable();

            if (eventParseCallback.shouldSkip(database, tableName)) {
                skipLoop = true;
                return;
            }

            //！ 事务结束skipLoop = false， 当一个事务中，存在其他不相关的表，
            /**
             *  eventType=
             *   start, begin   [1.ANONYMOUS_GTID 2.QUERY]
             *          [ TABLE_MAP  WRITE_ROWS]  可能这个表要跳过
             *          [ TABLE_MAP  UPDATE_ROWS]   这个表不跳过，因此skipLoop = false!
             *   end            [3.XID]
             * */
            skipLoop = false;

            currentTable = new TableData(database, tableMapEventData.getTableId(), tableName);
            eventParseCallback.onTableMapEvent(currentTable);
        } else if (EventType.isWrite(eventType)) {
            canFetchData = true;
            isInsert = true;
        } else if (EventType.isDelete(eventType)) {
            canFetchData = true;
            isDelete = true;
        } else if (EventType.isUpdate(eventType)) {
            canFetchData = true;//  ((UpdateRowsEventData)eventData).getTableId();
            isUpdate = true;
        } else if (eventType == EventType.XID) {
            //一个事务的结束
            EventHeaderV4 headerV4 = (EventHeaderV4) header;
            skipLoop = false;
            currentTable = null;

            //1个事务完成时才结束，设置为下一个事务，作为续传的起点       XidEventData
            eventParseCallback.onEndOfTransaction(txTime, headerV4.getPosition(), headerV4.getNextPosition());

        } else if (eventType == EventType.ROTATE) {
            RotateEventData rotateEventData = (RotateEventData) eventData;
            log.info("{} task[{}] reading new BinlogFile : {} - {} at {}", taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(),
                rotateEventData.getBinlogFilename(), rotateEventData.getBinlogPosition(), simpleDateFormat.format(header.getTimestamp()));
            // 开始读取新的binlog 文件，立刻触发进度保存。 其他事件没有fileName 信息...
            // 当一个新的连接建立成功，也是触发此事件。
            eventParseCallback.onReadingNewBinlogFileOrNewConnectionEstablished(txTime,
                rotateEventData.getBinlogFilename(), rotateEventData.getBinlogPosition());
        } else {
            // 建立连接的 时候
            // EventType.PREVIOUS_GTIDS
            // EventType.FORMAT_DESCRIPTION
            //log.info("{} task[{}] other event : {}", taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(), event.toString());
        }

        if (skipLoop || !canFetchData) {
            // EventType.XID 事件时设置 skipLoop = false，保证只是此次的事件都不处理。
            return;
        }

        if (currentTable == null) {
            throw new RuntimeException("===== no table data specified... current event is " + event.toString());
        }

        EventHeaderV4 headerV4 = (EventHeaderV4) header;

        // 表的列信息
        try {
            Method method = eventData.getClass().getMethod("getRows");
            Object data = method.invoke(eventData);

            eventParseCallback.onDML(txTime, headerV4.getNextPosition(), currentTable, data, eventType.name(), isUpdate, isInsert);
        } catch (Exception e) {
            log.error(String.format("getRows fail,exp=%s,event=%s", e.getMessage(), event.toString()), e);
            throw new RuntimeException("getRows fail of event : " + eventType.toString());
        }
    }


}
