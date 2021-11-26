package org.learn.binlogconnector;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.learn.binlogconnector.bean.CollectorTaskType;
import org.learn.binlogconnector.bean.TaskStatus;
import org.learn.binlogconnector.task.DataHandler;
import org.learn.binlogconnector.task.Task;
import org.learn.binlogconnector.task.TaskCallback;

import java.text.SimpleDateFormat;

@Slf4j
public abstract class AbstractTaskImpl<P, PR> implements Task<P, PR>, Runnable {

    /**
     * DefaultTaskManagerService 中 任务缓存的key
     * */
    protected final String taskKey;

    /**
     * zk 上的任务名
     * */
    protected String taskName;

    /**
     * 任务唯一标识，日志记录使用
     * */
    @Getter
    protected String uniqueTask;

    @Getter
    protected CollectorTaskType taskType;

    protected TaskStatus status;

    @Setter
    protected String msg;

    protected volatile boolean shutDownFlag = false;

    protected TaskCallback taskCallBack;

    protected DataHandler dataHandler;

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected AbstractTaskImpl(String taskKey) {
        this.taskKey = taskKey;
    }

    public void setRejected() {
        this.status = TaskStatus.REJECTED;
    }

    protected abstract void stopTask();



    @Override
    public void stop() {
        this.shutDownFlag = true;
        this.status = TaskStatus.DONE;

        stopTask();
        taskCallBack.onStop(taskKey, taskName);
    }

}
