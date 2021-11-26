package org.learn.binlogconnector.bean;

import lombok.Data;

@Data
public class CreateTableSqlInfo {

    private String table;

    private String createSql;
}
