package org.learn.binlogconnector.task;

import org.learn.binlogconnector.bean.TaskStatus;

/**
 * <pre>
 *
 * </pre>
 *
 */
public interface Task<P, PR> {

    /**
     * 定时任务间隔执行时间
     * */
    int TASK_SCHEDULE_DELAY_SECONDS = 2;

    <D> void start(P param, DataHandler<D> dataHandler, TaskCallback taskCallBack);

    <R> R update(P param);

    /**
     * 停止任务
     */
    void stop();

    /**
     * 销毁任务
     */
    void stopNow();

    /**
     * 获取任务当前最新进度信息
     *
     * @return
     */
    PR getProgress();

    /**
     * 获取初始化参数
     *
     * @return
     */
    P getParam();

    /**
     * 获取对应的数据处理器
     *
     * @return
     */
    DataHandler getDataHandler();


    /**
     * 获取当前任务状态
     *
     * @return
     */
    TaskStatus getStatus();

    /**
     * 是否为无界任务
     *
     * @return
     */
    boolean isUnbounded();

    String uniqueTask(P param);
}
