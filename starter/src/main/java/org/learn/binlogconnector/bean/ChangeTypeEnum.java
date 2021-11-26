package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChangeTypeEnum {
    //DDL("DDL", "DDL"),
    //不能明确操作含义的，但数据内容本身属于业务数据的，可同意归类为DATA类型
    DATA("DATA", "其他"),
    INSERT("INSERT", "新增"),
    UPDATE("UPDATE", "修改"),
    DELETE("DELETE", "删除");
    private String val;
    private String name;

    public boolean is(ChangeTypeEnum changeType) {
        return this == changeType;
    }
}
