package org.learn.binlogconnector;

import com.alibaba.fastjson.JSONObject;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.learn.binlogconnector.bean.*;
import org.learn.binlogconnector.db.DataSourceEnum;
import org.learn.binlogconnector.db.TableRepository;
import org.learn.binlogconnector.db.TargetBinlogRepository;
import org.learn.binlogconnector.task.BinlogEventParser;
import org.learn.binlogconnector.task.DataHandler;
import org.learn.binlogconnector.task.ProgressManager;
import org.learn.binlogconnector.task.TaskCallback;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 */
@Slf4j
public class BinlogTaskImpl extends AbstractTaskImpl<BinlogParam, BinlogPosition> {

    private BinaryLogClient binaryLogClient;

    //任务创建时是用于查询binlog信息，事件处理需要查询建表sql
    private final TargetBinlogRepository targetBinlogRepository;

    //业务库表操作
    private final TableRepository tableOperate;

    private final ProgressManager progressManager;

    /**
     * 获取任务监听binlog时，使用的位点信息方式
     */
    private TaskStartPositionStatusEnum taskStartPositionStatus;

    private BinlogPositionTaskInfo taskInfo;

    private final Map<String, Set<String>> dbTableSetMap = new HashMap<>();


    public BinlogTaskImpl(String taskKey, TargetBinlogRepository targetBinlogQuery,
                          ProgressManager progressManager, TableRepository tableOperate) {
        super(taskKey);
        this.progressManager = progressManager;
        this.tableOperate = tableOperate;
        this.targetBinlogRepository = targetBinlogQuery;
    }

    public BinlogTaskInfo buildBinlogTaskInfo() {
        return new BinlogTaskInfo(uniqueTask, status, taskStartPositionStatus, msg, dbTableSetMap, taskInfo);
    }

    @Override
    public String uniqueTask(BinlogParam param) {
        DatabaseConfig connection = param.getDataSourceEnum().getDatabaseConfig();
        return new StringBuilder(connection.getHostname()).append(":").append(connection.getPort()).toString();
    }

    public boolean removeTable(String database) {
        Set<String> tables = dbTableSetMap.remove(database);
        return tables == null || !tables.isEmpty();
    }

    /**
     * 根据查询 collect_task_progress时需要的参数，来创建对象
     */
    public static BinlogTaskImpl buildQueryTask(DataSourceEnum dataSourceEnum, ProgressManager progressManager, TableRepository tableOperate) {
        BinlogTaskImpl task = new BinlogTaskImpl(dataSourceEnum.uniqueDBServer(), null, progressManager, tableOperate);
        BinlogParam binlogParam = new BinlogParam("", dataSourceEnum, null);

        task.uniqueTask = task.uniqueTask(binlogParam);
        task.taskType = CollectorTaskType.BINLOG_REALTIME;
        return task;
    }


    @Override
    public BinlogTaskInfo update(BinlogParam param) {
        DatabaseConfig connection = param.getDataSourceEnum().getDatabaseConfig();
        Set<String> newTableSet = param.getTableSet();
        //update
        log.info("update task[{}] key : {}, db : {}, tables : {}", taskName, taskKey, connection.getDatabase(), newTableSet);
        if (TaskStatus.RUNNING == status) {
            log.info("update running task : {}", taskKey);
        }

        String database = connection.getDatabase();
        Set<String> tableSet = dbTableSetMap.get(database);
        if (tableSet == null) {
            dbTableSetMap.put(database, newTableSet);
        } else {
            tableSet.addAll(newTableSet);
        }

        taskStartPositionStatus = TaskStartPositionStatusEnum.EXIST_TASK;
        return buildBinlogTaskInfo();
    }


    private static final int RETRY_COUNT = 3;
    private static final int BINLOG_MAX_LIMIT = 11;

    private BinlogPosition getLatestBinlogThrowNotFoundException() {
        BinlogPosition binlogPosLatest = targetBinlogRepository.getLatestBinlog();

        // ! 如果查询到的binlog正好是中间位点，那么...则没法完整的按事务执行，比如缺少了tableMap事件，不知道操作的数据库和表
        BinlogPosition binlogPositionWithGtid = null;

        // 当limit 11 * 3仍然无法拉取到最新的gtid的位点，需要任务重试
        for (int i = 0; i < RETRY_COUNT; i++) {
            long start = binlogPosLatest.getPosition() + BINLOG_MAX_LIMIT * i;

            List<BinlogPos> binlogPosList = targetBinlogRepository
                    .getBinlogPos(binlogPosLatest.getFileName(), start, BINLOG_MAX_LIMIT * i, BINLOG_MAX_LIMIT);
            if (binlogPosList.isEmpty()) {
                //此时正好db没有数据更新...
                binlogPositionWithGtid = binlogPosLatest;
                log.info("find latest binlog when no change happens : {}, {}", binlogPositionWithGtid.getFileName(),
                        binlogPositionWithGtid.getPosition());
                break;
            }

            // Anonymous_Gtid
            Optional<BinlogPos> binlogPos = binlogPosList.stream().filter(item -> item.getEventType().toLowerCase().contains(BinlogPos.GTID))
                    .findFirst();
            if (binlogPos.isPresent()) {
                BinlogPos latest = binlogPos.get();
                binlogPositionWithGtid = new BinlogPosition(latest.getLogFile(), latest.getPosition());
                log.info("find latest binlog at : {}, {} in loop : {}", latest.getLogFile(), latest.getPosition(), i + 1);
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
        }

        if (binlogPositionWithGtid == null) {
            msg = "无法获取目标库最新的binlog";
            status = TaskStatus.DESTROYED;
            throw new RuntimeException("无法获取目标库最新的binlog : " + uniqueTask);
        }
        return binlogPositionWithGtid;
    }

    /**
     * 若不指定位点信息，或者指定的位点信息DB库不存在，则： 按DB服务最新的位点信息开始订阅
     */
    @Override
    public <D> void start(BinlogParam binlogParam, DataHandler<D> dataHandler, TaskCallback taskCallBack) {
        DatabaseConfig databaseConfig = binlogParam.getDataSourceEnum().getDatabaseConfig();
        this.uniqueTask = uniqueTask(binlogParam);
        this.taskName = binlogParam.getTaskName();
        this.taskCallBack = taskCallBack;

        List<CollectTaskProgress> collectTaskProgress = progressManager.getTaskProgress(CollectorTaskType.BINLOG_REALTIME.getType(), uniqueTask);

        BinlogPosition currentBinlogPosition;
        if (CollectionUtils.isEmpty(collectTaskProgress)) {
            //realtime
            BinlogPosition position = getLatestBinlogThrowNotFoundException();
            currentBinlogPosition = new BinlogPosition(position.getFileName(), position.getPosition());

            taskType = CollectorTaskType.BINLOG_REALTIME;
            taskStartPositionStatus = TaskStartPositionStatusEnum.LATEST;
        } else {
            //specified
            String progress = collectTaskProgress.get(0).getProgress();
            JSONObject taskProgress = JSONObject.parseObject(progress);
            String fileName = taskProgress.getString(BinlogPosition.FILENAME);
            Long position = taskProgress.getLong(BinlogPosition.POSITION);

            currentBinlogPosition = new BinlogPosition(fileName, position);
            if (StringUtils.isEmpty(currentBinlogPosition.getFileName()) || currentBinlogPosition.getPosition() == null) {
                throw new RuntimeException("binlog 位点信息不存在");
            }

            boolean ret = targetBinlogRepository.checkBinlog(currentBinlogPosition.getFileName(), currentBinlogPosition.getPosition().longValue());
            if (ret) {
                taskType = CollectorTaskType.BINLOG_SPECIFIED;
                taskStartPositionStatus = TaskStartPositionStatusEnum.ASSIGN_POINT;
                log.info("{} task[{}] resume execute from last stored binlog position :[{}, {}]", taskType.getTypeName(), uniqueTask,
                        currentBinlogPosition.getFileName(), currentBinlogPosition.getPosition());
            } else {
                taskType = CollectorTaskType.BINLOG_REALTIME;
                taskStartPositionStatus = TaskStartPositionStatusEnum.LATEST;
                log.info("{} task[{}] last stored binlog position invalid... try to get latest", taskType.getTypeName(), uniqueTask);

                currentBinlogPosition = getLatestBinlogThrowNotFoundException();
            }
        }

        String database = binlogParam.getDataSourceEnum().getDatabase();
        dbTableSetMap.put(database, binlogParam.getTableSet());
        status = TaskStatus.INIT;
        taskInfo = new BinlogPositionTaskInfo(currentBinlogPosition.getFileName(), currentBinlogPosition.getPosition(),
                taskType == CollectorTaskType.BINLOG_REALTIME, taskType.getTypeName(), uniqueTask);

        // schema – 参数只用于认证，和事件过滤无关，默认是整个server的
        BinaryLogClient binaryLogClient = new BinaryLogClient(databaseConfig.getHostname(), databaseConfig.getPort(), null,
                databaseConfig.getUsername(), databaseConfig.getPassword());
        binaryLogClient.setBinlogFilename(currentBinlogPosition.getFileName());
        binaryLogClient.setBinlogPosition(currentBinlogPosition.getPosition());

        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG
                // varchar类型的编码集是一致的话，就可以直接返回数据。
                //,EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        binaryLogClient.setEventDeserializer(eventDeserializer);

        BinlogDataAdapter binlogDataAdapter = new BinlogDataAdapter(dataHandler);

        // callback
        BinlogEventParseCallback eventParseCallback = new AbstractBinlogEventParseCallbackImpl(taskInfo, tableOperate, targetBinlogRepository,
                binlogDataAdapter) {
            @Override
            public boolean shouldSkip(String database, String tableName) {
                Set<String> tableSet = dbTableSetMap.get(database);
                return tableSet == null || !tableSet.contains(tableName);
            }

            @Override
            public void onReadingNewBinlogFileOrNewConnectionEstablished(long txTime, String binlogFilename, long binlogPosition) {
                taskInfo.setTxTime(txTime);
                taskInfo.setFileName(binlogFilename);
                taskInfo.setPosition(binlogPosition);
                try {
                    // 建立新连接，或者读取新的binlog文件
                    operateProgressThrowsException();//binlogTaskProgressService.upda teAndSaveProgress()
                } catch (Exception e) {
                    log.error("读取binlog文件或建立binlog同步链接失败：{},{},{}", txTime, binlogFilename, binlogPosition, e);
                    setTaskDeserializeException(e.getMessage());
                }
            }

            @Override
            public void onEndOfTransaction(long txTime, long currentPosition, long nextPosition) {
                taskInfo.setTxTime(txTime);
                taskInfo.setPosition(nextPosition);
                BinlogCollectTaskProgress collectTaskProgress = buildProgress(taskInfo);
                progressManager.updateProgress(collectTaskProgress, true);// binlogTaskProgressService.updateProgress()
            }

            // onDDL执行完成后执行进度更新
            @Override
            public void updateAndSaveTaskInfoAfterDDL(long txTime, long nextPosition) {
                taskInfo.setTxTime(txTime);
                taskInfo.setPosition(nextPosition);
                operateProgressThrowsException();// binlogTaskProgressService.upda teAndSaveProgress()
            }

            @Override
            public void onDDL(long txTime, long nextPosition, String database, String tableName, String originalSql) {
                try {
                    super.onDDL(txTime, nextPosition, database, tableName, originalSql);
                } catch (Exception e) {
                    log.error("处理DDL变更失败：{},{},{},{},{}", txTime, nextPosition, database, tableName, originalSql, e);
                    setTaskDeserializeException(e.getMessage());
                }
            }

            @Override
            public void onEventParseException(Event event, Exception e) {
                log.error("解析或处理binlog事件失败：{}", event, e);
                setTaskDeserializeException(e.getMessage());
            }
        };
        BinlogEventParser binlogEventParser = new BinlogEventParser(taskInfo, eventParseCallback);
        binaryLogClient.registerEventListener(event -> binlogEventParser.parseEvent(event));

        try {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(100));
        } catch (InterruptedException e) {
        }
        int time = Integer.parseInt(new SimpleDateFormat("mmssSSS").format(new Date()));
        // 每个客户端需要不同的serverId。
        binaryLogClient.setServerId(time);

        binaryLogClient.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                log.info("{} task[{}] connect success", taskType.getTypeName(), uniqueTask);
                setTaskSuccessAndScheduleSave("success");
            }

            @Override
            public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
                log.error("{} task[{}] onCommunicationFailure : {}, latest progress : [{}, {}]", taskType.getTypeName(), uniqueTask, ex.getMessage(),
                        taskInfo.getFileName(), taskInfo.getPosition());

                String host = "unknown";
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    host = inetAddress.getHostAddress() + '-' + inetAddress.getHostName();
                } catch (UnknownHostException e) {
                    log.warn("获取主机地址失败", e);
                }
                setTaskErrorAndThrowException(
                        "... binlog communicate fail : " + uniqueTask + ", running server : " + host + " , for : " + ex.getMessage());
            }

            @Override
            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
                log.error("{} task[{}] onEventDeserializationFailure : {}", taskType.getTypeName(), uniqueTask, ex.getMessage());
                setTaskErrorAndThrowException(ex.getMessage());
            }

            @Override
            public void onDisconnect(BinaryLogClient client) {
                log.info("{} task[{}] disconnect, may due to taskException...", taskType.getTypeName(), uniqueTask);
            }
        });

        this.binaryLogClient = binaryLogClient;
        log.info("{} task[{}] started, serverId : {}, db : {}, tables : {}, at file : {}, pos : {} ", taskType.getTypeName(), uniqueTask, time,
                database,
                binlogParam.getTableSet(), currentBinlogPosition.getFileName(), currentBinlogPosition.getPosition());

        progressManager.setTaskInfo(taskType.getTypeName(), uniqueTask);
        BinlogCollectTaskProgress progressStart = buildProgress(taskInfo);
        progressManager.saveProgress(progressStart);
    }


    @Override
    public void run() {
        //block
        status = TaskStatus.RUNNING;
        try {
            binaryLogClient.connect();
        } catch (IOException e) {
            log.error("{} task[{}] connect exception : {}", taskType.getTypeName(), uniqueTask, e.getMessage());
            setTaskErrorAndThrowException("binlog连接异常：" + e.getMessage());
        }

        //new Thread
        //binaryLogClient.connect(connectionTimeOut);
    }

    private void setTaskErrorAndThrowException(String msg) {
        this.shutDownFlag = true;
        this.status = TaskStatus.DESTROYED;
        this.msg = taskType.getTypeName() + "任务异常: " + msg;
        stopTask();

        taskCallBack.onTaskError(uniqueTask, taskName);
        progressManager.stopProgressing();
        throw new RuntimeException(this.msg);
    }

    /**
     * DDL失败了下线。
     */
    private void setTaskDeserializeException(String msg) {
        this.msg = taskType.getTypeName() + "任务异常: " + msg;
        taskCallBack.onDeSerializeFail(taskKey, taskName);
        progressManager.stopProgressing();  // binlogTaskProgressService.stopProgressing();
    }


    @Override
    protected void stopTask() {
        try {
            if (binaryLogClient != null) {
                binaryLogClient.disconnect();
                binaryLogClient = null;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        log.info("{} task[{}] receive stop, current pos : [{} - {}]", taskType.getTypeName(), uniqueTask, taskInfo.getFileName(),
                taskInfo.getPosition());
        super.stop();

        progressManager.stopProgressing();
        log.info("{} task[{}] stopped at {}, latest pos : [{}- {}]", taskType.getTypeName(), uniqueTask, dateFormat.format(new Date()),
                taskInfo.getFileName(), taskInfo.getPosition());
    }

    @Override
    public void stopNow() {
        log.info("{} task[{}] receive immediately stop, current pos : [{} - {}]", taskType.getTypeName(), uniqueTask, taskInfo.getFileName(),
                taskInfo.getPosition());
        super.stop();
        log.info("{} task[{}] stopped immediately at {}", taskType.getTypeName(), uniqueTask, dateFormat.format(new Date()));
    }

    @Override
    public BinlogPosition getProgress() {
        return taskInfo;
    }

    @Override
    public BinlogParam getParam() {
        return null;
    }

    @Override
    public DataHandler getDataHandler() {
        return dataHandler;
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public boolean isUnbounded() {
        return false;
    }

    public TaskStartPositionStatusEnum getTaskStartPositionStatus() {
        return taskStartPositionStatus;
    }

    private void setTaskSuccessAndScheduleSave(String msg) {
        this.status = TaskStatus.RUNNING;
        this.msg = msg;

        progressManager.startProgressing();
    }


    private BinlogCollectTaskProgress buildProgress(BinlogPositionTaskInfo taskInfo) {
        BinlogCollectTaskProgress collectTaskProgress = new BinlogCollectTaskProgress();
        collectTaskProgress.setType(taskType.getType());
        String unique = taskInfo.getUniqueTask();
        collectTaskProgress.setName(unique);

        collectTaskProgress.setTaskName(taskName);
        collectTaskProgress.setBinlogFile(taskInfo.getFileName());
        collectTaskProgress.setPosition(taskInfo.getPosition());
        collectTaskProgress.setTxTime(new Date(taskInfo.getTxTime()));

        JSONObject progress = new JSONObject();
        progress.put(BinlogPosition.FILENAME, taskInfo.getFileName());
        progress.put(BinlogPosition.POSITION, taskInfo.getPosition());
        collectTaskProgress.setProgress(progress.toJSONString());
        collectTaskProgress.setCreateTime(new Date());

        String meta = dbTableSetMap.keySet().stream().collect(Collectors.joining(", ", "[", "]"));
        collectTaskProgress.setMeta(meta);
        return collectTaskProgress;
    }

    /**
     * 仅限于异常场景： 因需要触发realTime的binlog任务，从而删除进度
     */
    public int deleteProgress() {
        return progressManager.deleteProgress(taskType.getType(), uniqueTask);
    }


    private void operateProgressThrowsException() {
        BinlogCollectTaskProgress collectTaskProgress = buildProgress(taskInfo);

        int update = progressManager.updateAndSaveProgress(collectTaskProgress);
        if (update != 1) {
            // 当一个重复的任务抛出RuntimeException异常或错误时，它将被放置在Future中，并且不会再重复该任务。
            log.error("{} task[{}] 保存 位点进度失败", taskType.getTypeName(), uniqueTask);
            throw new RuntimeException("保存 {} task[{}] 位点进度失败");
        }

    }

}
