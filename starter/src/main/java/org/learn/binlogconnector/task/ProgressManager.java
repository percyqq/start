package org.learn.binlogconnector.task;


import org.learn.binlogconnector.bean.CollectTaskProgress;

import java.util.List;

/**
 * 每个任务独享。
 * */
public interface ProgressManager {

    void setTaskInfo(String taskTypeName, String uniqueTask);

    void startProgressing();

    void stopProgressing();

    void commitProgress();

    List<CollectTaskProgress> getTaskProgress(int taskType, String taskName);

    int deleteProgress(int type, String taskName);

    /**
     * 保存，持久化
     * */
    int saveProgress(CollectTaskProgress progress);

    /**
     * 更新进度
     * @param needUpdateRecord 是否需要记录进度
     * */
    void updateProgress(CollectTaskProgress collectTaskProgress, boolean needUpdateRecord);

    /**
     * 更新并保存进度
     * */
    int updateAndSaveProgress(CollectTaskProgress collectTaskProgress);
}
