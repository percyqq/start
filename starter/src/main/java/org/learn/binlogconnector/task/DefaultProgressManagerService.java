package org.learn.binlogconnector.task;

import lombok.extern.slf4j.Slf4j;
import org.learn.binlogconnector.bean.CollectTaskProgress;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class DefaultProgressManagerService implements ProgressManager {

    protected final ProgressService progressService;

    protected final ReentrantLock lock = new ReentrantLock();
    protected volatile boolean changed = false;

    protected ScheduledFuture future;

    protected CollectTaskProgress taskProgress;

    protected String taskTypeName;
    protected String uniqueTask;

    /**
     * 每隔多少秒执行
     */
    protected final int taskScheduleDelaySeconds;
    protected final int logPrintCountMax;
    protected int logPrintCount = 0;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    protected DefaultProgressManagerService(ProgressService progressService, int delay, int logPrintCountMax) {
        this.progressService = progressService;
        this.taskScheduleDelaySeconds = delay;
        this.logPrintCountMax = logPrintCountMax;
    }


    @Override
    public void setTaskInfo(String taskTypeName, String uniqueTask) {
        this.taskTypeName = taskTypeName;
        this.uniqueTask = uniqueTask;
    }


    protected void cancelSchedule(ScheduledFuture future) {
        if (future != null) {
            if (!future.isCancelled()) {
                boolean result = future.cancel(false);
                log.info("{} task[{}] cancel scheduled result : {}", taskTypeName, uniqueTask, result);
            } else {
                log.info("{} task[{}] scheduled work has been canceled, [{}]", taskTypeName, uniqueTask);
            }
        } else {
            log.info("{} task[{}] no scheduled work set", taskTypeName, uniqueTask);
        }
    }

    @Override
    public void startProgressing() {
        log.info("{} task[{}] init schedule progress saving ", taskTypeName, uniqueTask);

        // 当一个重复的任务抛出RuntimeException异常或错误时，它将被放置在Future中，并且不会再重复该任务。
        this.future = scheduledExecutorService.scheduleWithFixedDelay(() -> commitProgress(),
                3, taskScheduleDelaySeconds, TimeUnit.SECONDS);
        onStartProgressing();
    }

    protected abstract void onStartProgressing();

    /**
     * 调用子任务
     */
    protected abstract void updateProgressRecord(CollectTaskProgress collectTaskProgress);

    protected abstract void onStopProgressing();

    @Override
    public void stopProgressing() {
        if (changed) {
            // 等待变更提交完成
            for (int i = 0; i < 3; i++) {
                if (changed) {
                    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(taskScheduleDelaySeconds));
                }
            }
        }

        if (changed) {
            log.error("{} task[{}] change has not committed until 6s", taskTypeName, uniqueTask);
        }

        cancelSchedule(this.future);
        onStopProgressing();
    }

    public void commitProgress() {
        logPrintCount++;
        if (changed) {
            if (logPrintCount > logPrintCountMax) {
                // 2min 打印一次
                logPrintCount = 0;
                log.info("commit progress : {}", taskProgress);
            }

            try {
                lock.lock();

                int update = progressService.saveProgress(taskProgress);
                if (update != 1) {
                    log.error("{} task[{}] 保存 位点进度失败", taskTypeName, uniqueTask);
                }

                taskProgress = null;
                changed = false;

            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public List<CollectTaskProgress> getTaskProgress(int taskType, String taskName) {
        return progressService.getTaskProgress(taskType, taskName);
    }

    @Override
    public int saveProgress(CollectTaskProgress progress) {
        return progressService.saveProgress(progress);
    }

    @Override
    public int deleteProgress(int type, String taskName) {
        log.info("{} task[{}] deleteProgress at {}, progress : {}", taskTypeName, uniqueTask, new Date(), taskProgress);
        return progressService.deleteProgress(type, taskName);
    }

    /**
     * @param needUpdateRecord  在一个DDL完成时，binlog需要更新记录点
     * */
    @Override
    public void updateProgress(CollectTaskProgress collectTaskProgress, boolean needUpdateRecord) {
        try {
            lock.lock();
            taskProgress = collectTaskProgress;
            changed = true;

            if (needUpdateRecord) {
                updateProgressRecord(collectTaskProgress);
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    public int updateAndSaveProgress(CollectTaskProgress collectTaskProgress) {
        int update;
        try {
            lock.lock();

            update = progressService.saveProgress(collectTaskProgress);
            if (update != 1) {
                log.error("{} task[{}] 保存 位点进度失败", taskTypeName, uniqueTask);
            }

            taskProgress = null;
            changed = false;
        } finally {
            lock.unlock();
        }

        return update;
    }


}
