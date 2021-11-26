package org.learn.binlogconnector.bean;

import lombok.Data;

/**
 *
 */
@Data
public class Column {

    private String tableSchema;

    private String tableName;

    private String columnName;

    private String dataType;

    private String columnDefault;

    private String columnKey;

    private String extra;

    private String columnComment;
}
