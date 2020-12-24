package org.learn.config.db2;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

@Slf4j
public class MyCustomerShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    private static final String SCM_CC_TASK_NAME = "scm_cc_task";
    private static final String SCM_CC_TASK_DETAIL_NAME = "scm_cc_task_detail";
    private static final int SCM_CC_TASK_COUNT = 8;
    private static final int SCM_CC_TASK_DETAIL_COUNT = 32;

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        Long brandId = preciseShardingValue.getValue();
        Long zoneId = brandId;// 根据rpc调用查询 当前brandId 对应的 zoneId。

        log.info("sharding zoneId: {}, collection:{}, preciseShardingValue:{}", zoneId, collection, preciseShardingValue);

        String currentCcTaskName = SCM_CC_TASK_NAME.concat("_").concat(zoneId % SCM_CC_TASK_COUNT + "");
        String currentCcTaskDetailName = SCM_CC_TASK_DETAIL_NAME.concat("_").concat(zoneId % SCM_CC_TASK_DETAIL_COUNT + "");
        for (String tableName : collection) {
            if (tableName.equals(currentCcTaskName)) {
                log.info("sharding table: {}", currentCcTaskName);
                return tableName;
            }
            if (tableName.equals(currentCcTaskDetailName)) {
                log.info("sharding table: {}", currentCcTaskName);
                return tableName;
            }
        }
        throw new IllegalArgumentException("error sharding ," + currentCcTaskName + " or " + currentCcTaskDetailName + "not exist");
    }
}
