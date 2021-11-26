package org.learn.binlogconnector.db;

import lombok.extern.slf4j.Slf4j;
import org.learn.binlogconnector.bean.TableColumn;
import org.learn.binlogconnector.db.DataSourceEnum;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * 业务库，辅助存储binlog相关的信息
 */
@Slf4j
public class TableRepository  {

    private final DataSourceEnum dataSourceEnum = DataSourceEnum.HDS;

    public void executeDDL(String sql) {
        dataSourceEnum.getMysqlSchemaMapper().executeDDL(sql);
    }

    public boolean checkTableExist(String tableName) {
        try (Connection conn = dataSourceEnum.getDataSource().getConnection()) {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            ResultSet tabs = dbMetaData.getTables(null, null, tableName, new String[]{"TABLE"});
            if (tabs.next()) {
                return true;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public List<TableColumn> getColumnNames(String table) {
        try {
            //String sql = "SHOW FULL COLUMNS FROM " + table;//不支持传参写法
            return dataSourceEnum.getMysqlSchemaMapper().getColumnNames(table);
        } catch (Exception e) {
            log.error(table + e.getMessage(), e);
        }
        return null;
    }

}
