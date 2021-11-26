package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableColumn {

    private String fieldName;
    private String fieldType;

    private String collation;
}
