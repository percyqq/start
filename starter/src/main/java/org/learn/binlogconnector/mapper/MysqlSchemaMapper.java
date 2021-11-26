package org.learn.binlogconnector.mapper;

import org.apache.ibatis.annotations.*;
import org.learn.binlogconnector.bean.Column;
import org.learn.binlogconnector.bean.CreateTableSqlInfo;
import org.learn.binlogconnector.bean.TableColumn;
import org.learn.binlogconnector.bean.TableIndexColumn;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 */
public interface MysqlSchemaMapper {

    @Update("${sql}")
    void executeDDL(@Param("sql") String sql);


    @Select("select * from  information_schema.columns where table_name= ${table}")
    List<Map<String, Object>> queryColumn(@Param("table") String table);

    @Select("show create table ${database}.${table}")
    @Results(value = {
        @Result(column = "Table", property = "table"),
        @Result(column = "Create Table", property = "createSql"),
    })
    CreateTableSqlInfo getCreateSql(@Param("database") String database, @Param("table") String table);

    @Select("SHOW INDEX FROM ${database}.${table} where Key_name = 'PRIMARY' ")
    @Results(id = "tableIndexColumnInfoMap", value = {
        @Result(column = "Seq_in_index", property = "seqIndex"),
        @Result(column = "Column_name", property = "columnName"),
    })
    List<TableIndexColumn> getPrimaryIndexColumns(@Param("database") String database, @Param("table") String table);


    @Select("select table_name,column_name,data_type,column_default,column_key,extra,column_comment "
        + "FROM information_schema.COLUMNS where table_schema=#{param1} and table_name=#{param2}")
    List<Column> getTableColumns(String tableSchema, String tableName);

    /**
     * 取得该表字段名 另外一种查询方式： SELECT table_schema, table_name, column_name, ordinal_position, column_type, COLLATION_NAME FROM information_schema.COLUMNS
     * WHERE  TABLE_NAME = ?    AND TABLE_SCHEMA = ?
     */
    @Select("SHOW FULL COLUMNS FROM ${table}")
    @Results(id = "tableColumnInfoMap", value = {
        @Result(column = "Field", property = "fieldName"),
        @Result(column = "Type", property = "fieldType"),
        @Result(column = "Collation", property = "collation"),
    })
    List<TableColumn> getColumnNames(@Param("table") String table);


}
