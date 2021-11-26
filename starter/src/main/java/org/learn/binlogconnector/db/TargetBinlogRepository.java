package org.learn.binlogconnector.db;

import lombok.extern.slf4j.Slf4j;
import org.learn.binlogconnector.bean.BinlogPos;
import org.learn.binlogconnector.bean.BinlogPosition;
import org.learn.binlogconnector.db.DataSourceEnum;
import org.springframework.dao.DataAccessException;

import java.util.List;

/**
 * 每个业务库都有对应的一个bean
 */
@Slf4j
public class TargetBinlogRepository  {

    private final DataSourceEnum dataSourceEnum;

    public TargetBinlogRepository(DataSourceEnum dataSourceEnum) {
        this.dataSourceEnum = dataSourceEnum;
    }


    public String getCreateSql(String database, String table) {
        return dataSourceEnum.getMysqlSchemaMapper().getCreateSql(database, table).getCreateSql();
    }

    public BinlogPosition getLatestBinlog() {
        return dataSourceEnum.getMysqlStatusMapper().getLatestBinlogPosition();
    }

    public List<BinlogPos> getBinlogPos(String fileName, long start, int offset, int limit) {
        return dataSourceEnum.getMysqlStatusMapper().getBinlogPos(fileName, start, offset, limit);
    }

    //Access denied; you need (at least one of) the REPLICATION SLAVE privilege(s) for this operation
    // limit 2 ： 如果是limit 1 ： 存在某个位点，导致可以查询出来的结果不报错..
    public boolean checkBinlog(String fileName, long position) {
        try {
            // jdbcTemplate.queryForList("show binlog events in 'mysql-bin.000003' from 123 limit 2;");
            dataSourceEnum.getMysqlStatusMapper().checkBinlog(fileName, position);

            return true;
        } catch (DataAccessException e) {
            log.warn(" current binlog file not exist or position error! " + e.getMessage(), e);
            return false;
        }
    }
}
