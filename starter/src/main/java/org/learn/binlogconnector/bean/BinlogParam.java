package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.learn.binlogconnector.db.DataSourceEnum;

import java.util.Set;

/**
 * <pre>
 *
 * </pre>
 *
 */
@Data
@AllArgsConstructor
public class BinlogParam {

    private String taskName;

    private DataSourceEnum dataSourceEnum;

    private Set<String> tableSet;
    /**
     * 为null，表示从新获取库当前binlog位点信息
     */
    //private BinlogPosition currentBinlogPosition;
}
