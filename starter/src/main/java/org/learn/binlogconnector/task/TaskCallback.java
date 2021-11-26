package org.learn.binlogconnector.task;


public interface TaskCallback {

    void onStop(String taskKey, String taskName);

    void onDeSerializeFail(String taskKey, String taskName);

    /**
     *  任务错误，退出
     * */
    void onTaskError(String taskKey, String taskName);
}
