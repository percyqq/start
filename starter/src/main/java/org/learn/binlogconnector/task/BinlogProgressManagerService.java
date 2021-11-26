package org.learn.binlogconnector.task;

import lombok.extern.slf4j.Slf4j;
import org.learn.binlogconnector.bean.BinlogCollectTaskProgress;
import org.learn.binlogconnector.bean.BinlogTaskProgressRecord;
import org.learn.binlogconnector.bean.CollectTaskProgress;
import org.springframework.context.annotation.Scope;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Scope("prototype")
public class BinlogProgressManagerService extends DefaultProgressManagerService implements ProgressManager {

    private ScheduledFuture future;

    private boolean updated = false;

    private BinlogTaskProgressRecord binlogTaskProgressRecord = new BinlogTaskProgressRecord();

    //private BinlogTaskProgressRepository binlogTaskProgressRepository;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private static final int recordDelay = 300;

    private BinlogProgressManagerService(ProgressService progressService,
                                         //BinlogTaskProgressRepository binlogTaskProgressRepository,
                                         int delay, int logPrintCountMax) {
        super(progressService, delay, logPrintCountMax);
        //this.binlogTaskProgressRepository = binlogTaskProgressRepository;
    }

    public static BinlogProgressManagerService buildBinlogProgressManager(ProgressService progressService,
                                                                          //BinlogTaskProgressRepository binlogTaskProgressRepository,
                                                                          int delay, int logPrintCountMax) {
        BinlogProgressManagerService manager = new BinlogProgressManagerService(progressService,
                //binlogTaskProgressRepository,
                delay, logPrintCountMax);
        return manager;
    }


    @Override
    protected void onStartProgressing() {
        log.info(" BinlogProgressManagerService {} start progressing record start at {}==", uniqueTask, new Date());
        this.future = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            //每分钟保存一下 事务的进度
            //saveProgressRecord();
        }, 1, recordDelay, TimeUnit.SECONDS);
    }

    private int saveProgressRecord() {
        if (updated) {
            int update = 1;// binlogTaskProgressRepository.save(binlogTaskProgressRecord);
            if (update == 1) {
                updated = false;
            } else {
                log.warn("BinlogTaskProgressRecordService save record failed!");
            }
            return update;
        } else {
            return 0;
        }
    }

    @Override
    protected void updateProgressRecord(CollectTaskProgress collectTaskProgress) {
        try {
            BinlogCollectTaskProgress progress = (BinlogCollectTaskProgress) collectTaskProgress;
            if (progress.getTxTime().getTime() < 10) {
                log.info("BinlogProgressManagerService binlog time receive 0... ignore ");
                return;
            }

            binlogTaskProgressRecord.setTaskName(progress.getTaskName());
            binlogTaskProgressRecord.setBinlogFile(progress.getBinlogFile());
            binlogTaskProgressRecord.setPosition(progress.getPosition());
            binlogTaskProgressRecord.setTxTime(progress.getTxTime());

            updated = true;
        } catch (Exception e) {
            log.error("BinlogProgressManagerService on update record error, " + e.getMessage(), e);
        }

    }


    @Override
    protected void onStopProgressing() {
        log.info("BinlogProgressManagerService onStopProgressing cancelSchedule at {}", new Date());
        cancelSchedule(this.future);
    }
}
