package org.learn.binlogconnector.bean;

import lombok.Data;

@Data
public class TableIndexColumn implements Comparable<TableIndexColumn> {

    private int seqIndex;
    private String columnName;

    @Override
    public int compareTo(TableIndexColumn o) {
        return Integer.compare(seqIndex, o.seqIndex);
    }
}
