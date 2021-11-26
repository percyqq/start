package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;

/**
 * <pre>
 *
 * </pre>
 *
 */
@AllArgsConstructor
public enum TaskStartPositionStatusEnum {
    EARLIEST("基于DB服务最早的位点"),
    ASSIGN_POINT("基于指定的位点"),
    EXIST_TASK("对应库已经存在任务时"),
    LATEST("基于DB服务最新的位点"),
    ;
    private String name;

    public boolean is(TaskStartPositionStatusEnum status) {
        return this == status;
    }
}
