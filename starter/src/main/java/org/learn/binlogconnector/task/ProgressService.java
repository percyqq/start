package org.learn.binlogconnector.task;

import org.learn.binlogconnector.bean.CollectTaskProgress;

import java.util.List;

/**
 * 业务库，辅助存储binlog相关的信息
 */
public interface ProgressService {

    List<CollectTaskProgress> getTaskProgress(int taskType, String unique);

    int saveProgress(CollectTaskProgress collectTaskProgress);

    int deleteProgress(int type, String name);
}
