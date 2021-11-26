package org.learn.binlogconnector;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.learn.binlogconnector.bean.BinlogPositionTaskInfo;
import org.learn.binlogconnector.bean.TableColumn;
import org.learn.binlogconnector.bean.TableData;
import org.learn.binlogconnector.db.TableRepository;
import org.learn.binlogconnector.db.TargetBinlogRepository;
import org.learn.binlogconnector.util.TableDDLMatcherUtil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractBinlogEventParseCallbackImpl implements BinlogEventParseCallback {

    private static final int SYNC_TYPE_TABLE_MAP = 1;
    private static final int SYNC_TYPE_GET_SLAVE_COLUMN = 2;
    private static final int SYNC_TYPE_UPDATE_SLAVE_COLUMN = 3;
    private static final int SYNC_TYPE_DDL_LOST = 4;

    private final BinlogPositionTaskInfo taskInfo;

    private final TableRepository tableOperate;

    private final TargetBinlogRepository targetBinlogQuery;

    private final BinlogDataAdapter binlogDataAdapter;

    /**
     * 目标库的 ： schema + table 作为唯一key 同步方需要保存 监听数据库的表结构，变更时同步处理
     */
    private Map<String, TableData> dbTableMap = new HashMap<>();

    private Map<String, List<TableColumn>> columnNamesMap = new HashMap<>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AbstractBinlogEventParseCallbackImpl(BinlogPositionTaskInfo taskInfo, TableRepository tableOperate,
                                                TargetBinlogRepository targetBinlogQuery, BinlogDataAdapter binlogDataAdapter) {
        this.taskInfo = taskInfo;
        this.tableOperate = tableOperate;
        this.targetBinlogQuery = targetBinlogQuery;
        this.binlogDataAdapter = binlogDataAdapter;
    }


    /**
     * @return synced 是否触发了同步表
     */
    private boolean syncTable(TableData tableData, int i) {
        String key = TableDDLMatcherUtil.buildTempTable(tableData.getDatabase(), tableData.getTable());//tableData.buildTableKey();
        TableData cacheTable = dbTableMap.get(key);

        boolean synced = false;
        if (cacheTable == null) {
            cacheTable = tableData;

            // 监听目标库没有temp前缀，slave库有temp前缀
            boolean exist = tableOperate.checkTableExist(key);
            if (!exist) {
                if (log.isDebugEnabled()) {
                    log.debug("{} task[{}] sync table : {}, by type : no table exist", taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(), key);
                }
                // 刚开始启动的任务不存在表
                String createTableSql = targetBinlogQuery.getCreateSql(cacheTable.getDatabase(), cacheTable.getTable());
                String slaveSql = TableDDLMatcherUtil.replaceCreateTableName(cacheTable.getDatabase(), createTableSql);

                tableOperate.executeDDL(slaveSql);
                synced = true;
            } else {
                String type = "";
                if (i == SYNC_TYPE_TABLE_MAP) {
                    type = " *** SYNC_TYPE_TABLE_MAP";
                } else if (i == SYNC_TYPE_GET_SLAVE_COLUMN) {
                    type = "SYNC_TYPE_GET_SLAVE_COLUMN";
                    // 在获取列的时候触发了表的同步创建流程，需要回溯这种场景
                    log.warn("check this scene of table : {}, should not reach here... ", tableData);
                } else if (i == SYNC_TYPE_UPDATE_SLAVE_COLUMN) {
                    type = "SYNC_TYPE_UPDATE_SLAVE_COLUMN";
                } else if (i == SYNC_TYPE_DDL_LOST) {
                    type = "SYNC_TYPE_DDL_LOST";
                }
                if (log.isDebugEnabled()) {
                    log.debug("{} task[{}] sync table : {}, by type : {}", taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(), key, type);
                }
            }
            dbTableMap.put(key, cacheTable);
        }

        return synced;
    }


    private void tableSyncAndSaveProgressAndSetCurrentTableNull(long txTime, long nextPosition, String logStr, String originalSql, String executeSql,
        TableData tableData, String tableKey) {
        log.info("onDDL execute:{}-{} - originalSql-{}", tableData, executeSql, originalSql);
        String tableName = tableData.getTable();
        if (log.isDebugEnabled()) {
            log.debug("{} task[{}] [{}] match at {}, table[{}.{}], originalSql:[{}], final sql :[{}]", taskInfo.getTaskTypeName(),
                taskInfo.getUniqueTask(), logStr, simpleDateFormat.format(txTime), tableData.getDatabase(), tableName, originalSql, executeSql);
        }

        if (taskInfo.isRealTime()) {
            // todo realTime 删表的操作....
            // binlog 如果断点没法续上，使用实时binlog位点的场景时，如果此时收到DDL语句执行失败时，需要重新获取create table语句并同步
            try {
                tableOperate.executeDDL(executeSql);
            } catch (Exception e) {
                log.info("{} task[{}] realTime binlog..may lost ddl, try drop and reCreate table, error : ",
                    taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(), e.getMessage());
                dbTableMap.remove(tableKey);
                tableOperate.executeDDL("drop table if exists " + TableDDLMatcherUtil.buildTempTable(tableData.getDatabase(), tableName));
                syncTable(tableData, SYNC_TYPE_DDL_LOST);
            }
        } else {
            // 如果是指定位点，手动删除了表，需要报错。 这时从主库用 show create table获取到的表结构，可能已经和[历史时间(binlog触发的时间)]的表结构有了差异，
            // 这时需要重新全量，再删除对应的位点信息，跑 realTime的任务。

            boolean synced = syncTable(tableData, SYNC_TYPE_DDL_LOST);
            if (!synced) {
                //没有触发同步，说明表存在，执行DDL变更。 //场景：正在运行的binlog任务（非实时），一直没有数据触发表同步，新增的表/突然遇到DDL，此时需要
                tableOperate.executeDDL(executeSql);
            } else {
                log.warn("{} task[{}] specified position, tableSyncAndSaveProgressAndSetCurrentTableNull find no table, synced table [{} - {}]",
                    taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(), tableData.getDatabase(), tableName);
            }
        }

        updateAndSaveTaskInfoAfterDDL(txTime, nextPosition);
    }

    public abstract void updateAndSaveTaskInfoAfterDDL(long txTime, long nextPosition);

    @Override
    public void onTableMapEvent(TableData currentTable) {
        syncTable(currentTable, SYNC_TYPE_TABLE_MAP);
    }

    @Override
    public void onDDL(long txTime, long nextPosition, String database, String tableName, String originalSql) {
        log.info("onDDL begin:{}-{}-{}", database, tableName, originalSql);
        TableData tableData = new TableData(database, tableName);
        String key = TableDDLMatcherUtil.buildTempTable(database, tableName);

        //! DDL 只有GTID 和 QUERY事件，因此，当DDL执行时，立刻保存进度信息。下次重复执行时，避免在DDL之前的节点执行而发生错误
        //TableDDL.replaceXXXTableName 同步库执行sql变更。替换掉可能存在的 [库名.表名] ==> tmp_表名
        String targetExeSql = TableDDLMatcherUtil.matchAlterTableName(database, originalSql);
        if (targetExeSql != null) {
            //updateSlaveColumnNames(tableData, sql);
            columnNamesMap.remove(key);
            tableSyncAndSaveProgressAndSetCurrentTableNull(txTime, nextPosition, "DDL match", originalSql, targetExeSql, tableData, key);
            return;
        }

        targetExeSql = TableDDLMatcherUtil.matchDropTableName(database, originalSql);
        if (targetExeSql != null) {
            dbTableMap.remove(key);
            columnNamesMap.remove(key);
            tableSyncAndSaveProgressAndSetCurrentTableNull(txTime, nextPosition, "DROP table", originalSql, targetExeSql, tableData, key);
            return;
        }

        targetExeSql = TableDDLMatcherUtil.matchCreateTableName(database, originalSql);
        if (targetExeSql != null) {
            tableSyncAndSaveProgressAndSetCurrentTableNull(txTime, nextPosition, "CREATE table", originalSql, targetExeSql, tableData, key);
            return;
        }

        targetExeSql = TableDDLMatcherUtil.matchRenameTableName(originalSql);
        if (targetExeSql != null) {
            // pt-osc 采用建立新表执行变更，并最终替换旧表的操作方式，此时删除缓存的对应表和列，重新获取新的表结构。
            log.info("{} task[{}] meet pt-osc sql! sql : {}", taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(), originalSql);
            // 不需要保存进度
            dbTableMap.remove(key);
            columnNamesMap.remove(key);

            // rename table cirus.aa to aa_old;
            // rename table cirus.aa1 to aa;
            // 需要删除 temp_help 表，新的表是修改了列数据的，重新触发表同步流程！
            String executeSql = "drop table " + key;
            try {
                tableOperate.executeDDL(executeSql);
            } catch (Exception e) {
                log.info("{} task[{}] realTime binlog. try drop table, error : ",
                        taskInfo.getTaskTypeName(), taskInfo.getUniqueTask(), e.getMessage());
            }
            return;
        }
    }

    @Override
    public void onDML(long txTime, long nextPosition, TableData currentTable, Object data, String eventType, boolean isUpdate, boolean isInsert) {
        List<TableColumn> columnNames = getSlaveColumnNames(currentTable);
        int colSize = columnNames.size();

        log.info("binlog onDML:[{}],[{}:{}]],{}", eventType, currentTable.getDatabase(), currentTable.getTable(), JSON.toJSONString(data));

        if (isUpdate) {
            List<Map.Entry<Serializable[], Serializable[]>> value = (List<Map.Entry<Serializable[], Serializable[]>>) data;
            int size = value.get(0).getKey().length;
            if (colSize != size) {
                // DDL匹配表名失败后，只有这里才删除tmp，再创建
                log.warn("table columns disMatch ! [update] reSync table column data : {}", currentTable);
                syncTable(currentTable, SYNC_TYPE_TABLE_MAP);
                columnNames = getSlaveColumnNames(currentTable);
            }

            binlogDataAdapter.convertUpdateAndSend(value, currentTable, columnNames, txTime);

        } else {
            List<Serializable[]> value = (List<Serializable[]>) data;  //insert / delete 是 LinkedList
            int size = value.get(0).length;
            if (colSize != size) {
                log.warn("table columns disMatch ! [insert/delete] reSync table column data : {}", currentTable);
                syncTable(currentTable, SYNC_TYPE_TABLE_MAP);
                columnNames = getSlaveColumnNames(currentTable);
            }

            binlogDataAdapter.convertAndSend(isInsert, value, currentTable, columnNames, txTime);

        }
    }


    private List<TableColumn> getSlaveColumnNames(TableData table) {
        String key = TableDDLMatcherUtil.buildTempTable(table.getDatabase(), table.getTable());
        List<TableColumn> columns = columnNamesMap.get(key);
        if (columns == null) {
            syncTable(table, SYNC_TYPE_GET_SLAVE_COLUMN);

            // slave库有temp前缀    //是业务方的库，已经指定了库名，只传表名
            columns = tableOperate.getColumnNames(key);
            columnNamesMap.put(key, columns);
        }
        return columns;
    }


}
