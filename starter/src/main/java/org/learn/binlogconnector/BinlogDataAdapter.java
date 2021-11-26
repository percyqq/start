package org.learn.binlogconnector;

import lombok.extern.slf4j.Slf4j;
import org.learn.binlogconnector.bean.*;
import org.learn.binlogconnector.db.DataSourceEnum;
import org.learn.binlogconnector.task.DataHandler;
import org.learn.binlogconnector.util.DataDeserializerUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class BinlogDataAdapter {

    private final DataHandler dDataHandler;

    public BinlogDataAdapter(DataHandler dDataHandler) {
        this.dDataHandler = dDataHandler;
    }


    public void convertUpdateAndSend(List<Map.Entry<Serializable[], Serializable[]>> value,
                                     TableData currentTable, List<TableColumn> columnNames, long txTime) {
        // 1条update的 sql 同时更新了多条时， value是多条
        // 多条单行更新的sql 不会合并，还是单独分开的。
        List<Map<Map<String, Object>, Map<String, Object>>> datas = DataDeserializerUtil.deserializeMulti(columnNames, value);

        List<FeatureMessage> featureMessageList = datas.stream().map(rowData -> {
            //rowData只有一条数据 ==> Map<Old, New>,    Old : Map<String, Object>   ,   New : Map<String, Object>
            FeatureMessage featureMessage = new FeatureMessage();
            for (Map.Entry<Map<String, Object>, Map<String, Object>> entry : rowData.entrySet()) {
                Map<String, Object> oldData = entry.getKey();
                Map<String, Object> newData = entry.getValue();

                featureMessage.setOldFields(oldData);
                featureMessage.setData(newData);

                featureMessage.setChangeType(ChangeTypeEnum.UPDATE);
                featureMessage.setCollectType(CollectTypeEnum.BINLOG);

                String dbName = DataSourceEnum.getDBName(currentTable.getDatabase());
                featureMessage.setSource(dbName);
                featureMessage.setDataSet(currentTable.getTable());

                featureMessage.setCollectTime(new Date());
                featureMessage.setSourceTime(new Date(txTime));
            }

            return featureMessage;
        }).collect(Collectors.toList());
        //handle message
        handleMessage(featureMessageList);
    }


    public void convertAndSend(boolean isInsert, List<Serializable[]> value,
                               TableData currentTable, List<TableColumn> columnNames, long txTime) {
        //insert / delete 是 LinkedList
        List<Map<String, Object>> datas = DataDeserializerUtil.deserialize(columnNames, value);

        ChangeTypeEnum changeType = isInsert ? ChangeTypeEnum.INSERT : ChangeTypeEnum.DELETE;
        List<FeatureMessage> featureMessageList = datas.stream().map(row -> {
            FeatureMessage featureMessage = new FeatureMessage();
            featureMessage.setData(row);

            featureMessage.setChangeType(changeType);
            featureMessage.setCollectType(CollectTypeEnum.BINLOG);

            String dbName = DataSourceEnum.getDBName(currentTable.getDatabase());
            featureMessage.setSource(dbName);
            featureMessage.setDataSet(currentTable.getTable());

            featureMessage.setCollectTime(new Date());
            featureMessage.setSourceTime(new Date(txTime));

            return featureMessage;
        }).collect(Collectors.toList());
        //handle message
        handleMessage(featureMessageList);
    }

    private void handleMessage(List<FeatureMessage> featureMessageList) {
        featureMessageList.forEach(item -> {
            try {
                dDataHandler.handle(item);
            } catch (Exception e) {
                log.error("handleMessage error:{}", item, e);
            }
        });
    }

}
