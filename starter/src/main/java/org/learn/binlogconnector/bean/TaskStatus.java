package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 */
@AllArgsConstructor
@Getter
public enum TaskStatus {
    INIT("初始化", ""),
    RUNNING("执行中", ""),
    PAUSE("暂停", "支持断点续传"),

    REJECTED("拒绝执行", "任务超过数量"),
    //DESTROYING("销毁中", "有界任务执行完成后自动销毁｜手动销毁"),
    DESTROYED("已销毁", "有界任务执行完成｜手动销毁完成"),
    DONE("已完成", "任务已完成");

    private String name;
    private String desc;

    /**
     * 代表任务执行成功
     */
    public boolean isSuccess() {
        return this == INIT || this == RUNNING || this == DONE;
    }


    public boolean is(TaskStatus status) {
        return this == status;
    }
}
