package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CollectTypeEnum {
    BINLOG("BINLOG", "BINLOG"),
    SQL("SQL", "SQL"),
    MQ("MQ", "MQ");
    private String val;
    private String name;

    public boolean is(CollectTypeEnum collectTypeEnum) {
        return collectTypeEnum == this;
    }
}
