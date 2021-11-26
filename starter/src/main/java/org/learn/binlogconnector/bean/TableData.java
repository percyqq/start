package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableData {

    public TableData(String database, String table) {
        this.database = database;
        this.table = table;
    }

    private String database;

    private Long tableId;

    private String table;


}
