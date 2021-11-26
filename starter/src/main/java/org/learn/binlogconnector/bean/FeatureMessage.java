package org.learn.binlogconnector.bean;

import lombok.Data;
import org.learn.binlogconnector.db.DataSourceEnum;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 特征消息 库名  表名  data(map)
 */
@Data
public class FeatureMessage<M> implements Serializable {

    /**
     * 数据来源（任意一种） 数据库名 APP名 SQL采集任务名
     */
    private String source;

    /**
     * 数据集合名（任意一种） 表名 API名 SQL片段名 ...
     */
    private String dataSet;
    /**
     * 变更类型（任意一种） INSERT UPDATE DELETE DATA(不能明确操作含义的，但数据内容本身属于业务数据的，可同意归类为DATA类型) DDL
     */
    private ChangeTypeEnum changeType;

    /**
     * 采集类型（任意一种） BINLOG SQL DA API ...
     */
    private CollectTypeEnum collectType;

    /**
     * 源库/系统落地时间（比如binlog生成时间/事务提交时间）
     */
    private Date sourceTime;

    /**
     * 采集时间
     */
    private Date collectTime;

    /**
     * 开始处理时间(writer接收到日志时的时间)
     */
    private Date startHandleTime;

    /**
     * 处理完成时间(writer处理完成日志时的时间)
     */
    private Date endHandleTime;

    /**
     * 上个版本的数据内容（可选）
     */
    private Map<String, Object> oldFields;

    /**
     * required 最新完整数据内容
     */
    private Map<String, Object> data;

    /**
     * 自定义数据元信息
     */
    private M meta;

    /**
     * 保留key
     */
    private String serialNo;

    private Long offset;

    private String uuid;

    public FeatureMessage<M> copy() {
        FeatureMessage<M> featureMessage = new FeatureMessage<>();
        featureMessage.setData(data);
        featureMessage.setChangeType(changeType);
        featureMessage.setCollectTime(collectTime);
        featureMessage.setCollectType(collectType);
        featureMessage.setDataSet(dataSet);
        featureMessage.setMeta(meta);
        featureMessage.setOffset(offset);
        featureMessage.setOldFields(oldFields);
        featureMessage.setSerialNo(serialNo);
        featureMessage.setSource(source);
        featureMessage.setSourceTime(sourceTime);
        featureMessage.setUuid(uuid);
        return featureMessage;
    }

    public FeatureMessage<M> copy(Map<String, Object> data) {
        FeatureMessage message = this.copy();
        message.setData(data);
        return message;
    }

    public static final FeatureMessage newMessage(DataSourceEnum dataSourceEnum, String table) {
        FeatureMessage message = new FeatureMessage();
        message.setChangeType(ChangeTypeEnum.DATA);
        message.setSource(dataSourceEnum.name());
        message.setDataSet(table);
        message.setCollectType(CollectTypeEnum.SQL);

        Date date = new Date();
        message.setSourceTime(date);
        message.setCollectTime(date);
        return message;
    }
}
