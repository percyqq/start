package org.learn.canal;

import com.alibaba.fastjson.JSON;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.*;
import com.github.shyiko.mysql.binlog.io.ByteArrayInputStream;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class MysqlBinlogConnector {

    private static List<String> database;
    private static Map<String, List<TableColumn>> columnNamesMap = new HashMap<>();
    private static HashMap<Long, TableData> tableMap = new HashMap<>();

    private static JdbcTemplate jdbcTemplate;

    @Data
    @AllArgsConstructor
    private static class TableData {
        private String database;
        private String table;
    }

    @Data
    @AllArgsConstructor
    private static class TableColumn {
        private String name;
        private String type;
    }


    //注意检查ip变化！
    private static final String IP = "10.242.0.204";

    public static void main(String[] args) throws Exception {

        BinaryLogClient client = new BinaryLogClient(IP, 13306, "root", "20210112");


        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + IP + ":13306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
        config.setUsername("root");
        config.setPassword("20210112");
        DataSource dataSource = new HikariDataSource(config);
        jdbcTemplate = new JdbcTemplate(dataSource);

        //getColunms("collect_task_progress");


        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );

        // do not deserialize EXT_DELETE_ROWS event data, return it as a byte array
        eventDeserializer.setEventDataDeserializer(EventType.EXT_DELETE_ROWS,
                new ByteArrayEventDataDeserializer());

        // skip EXT_WRITE_ROWS event data altogether
        eventDeserializer.setEventDataDeserializer(EventType.EXT_WRITE_ROWS,
                new NullEventDataDeserializer());

        XidEventData d;//
        client.setEventDeserializer(eventDeserializer);
        client.registerEventListener(new BinaryLogClient.EventListener() {

            @Override
            public void onEvent(Event event) {
                EventData eventData = event.getData();
                EventHeader header = event.getHeader();

                EventType eventType = header.getEventType();
                // 表信息
                TableData table = null;
                List<TableColumn> columnNames = null;
                List<Map<String, Object>> datas = null;


                if (eventType == EventType.TABLE_MAP) {
                    if (eventData instanceof TableMapEventData) {
                        TableMapEventData tableMapEventData = (TableMapEventData) eventData;
                        if (tableMapEventData != null) {
                            // 没有监听的库不处理
//                            if (!database.contains(tableMapEventData.getDatabase())) {
//                                return;
//                            }
                            //log.info(" 时间:" + Calendar.getInstance().getTime() + ", tableId: " + tableMapEventData.getTableId() + ", tableName: " + tableMapEventData.getDatabase() + "." + tableMapEventData.getTable());
                            table = new TableData(tableMapEventData.getDatabase(), tableMapEventData.getTable());
                            tableMap.put(tableMapEventData.getTableId(), table);

                        }
                    }
                }

                if (eventType == EventType.EXT_WRITE_ROWS) {
                    WriteRowsEventData writeRowsEventData = (WriteRowsEventData) eventData;

                    if (writeRowsEventData != null) {
                        table = tableMap.get(writeRowsEventData.getTableId());
                        columnNames = getColumnNames(table);

                        System.out.println(writeRowsEventData);
                    }
                }

                boolean canGetData = true;
                if (eventType == EventType.TABLE_MAP) {
                    TableMapEventData updateData = (TableMapEventData) eventData;

                    table = tableMap.get(updateData.getTableId());
                    columnNames = getColumnNames(table);

                    //System.out.println(" .... TableMapEventData : " + updateData);
                    canGetData = false;
                }


                if (eventType == EventType.UPDATE_ROWS || eventType == EventType.EXT_UPDATE_ROWS) {
                    UpdateRowsEventData updateData = (UpdateRowsEventData) eventData;
                    //System.out.println(" ======> " + updateData.toString());


                    // 更新
//                    log.info(" 时间:{}，method:{}，tableName:{}，sqlParameter:{}", Calendar.getInstance().getTime(), "UPDATE", table,
//                            JSON.toJSONString(Arrays.asList(updateData.getRows())));
                    table = tableMap.get(updateData.getTableId());
                    columnNames = getColumnNames(table);


                } else {
                    //System.out.println(" ======> " + event.toString());
                }

                // update 没有rows 数据....
                if (canGetData) {
                    datas = rowsToMap(eventData, columnNames);

                }
                if (datas != null) {
                    System.out.println(" || datas: " + datas);
                }
                System.out.println("====> " + event.toString());
            }
        });
        client.connect();

    }

    private static List<TableColumn> getColumnNames(TableData table) {
        if (null != table && StringUtils.isNotBlank(table.getDatabase()) && StringUtils.isNotBlank(table.getTable())) {
            String key = table.getDatabase() + "." + table.getTable();
            if (!columnNamesMap.containsKey(key)) {
                columnNamesMap.put(key, getColumnNames(key));
            }
            return columnNamesMap.get(key);
        }
        return null;
    }

    /**
     * 取得该表字段名
     *
     * @param table
     * @return
     */
    public static synchronized List<TableColumn> getColumnNames(String table) {
        try {
            List<TableColumn> columnNames = jdbcTemplate.query("SHOW FULL COLUMNS FROM " + table, new RowMapper<TableColumn>() {
                @Override
                public TableColumn mapRow(ResultSet resultSet, int i) throws SQLException {
                    String name = resultSet.getString("Field");
                    String type = resultSet.getString("Type");

                    return new TableColumn(name, type);
                }
            });
            return columnNames;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 监听到的数据行转换为map
     *
     * @param rowsEventData
     * @param columnNames
     * @return
     */
    private static List<Map<String, Object>> rowsToMap(Object rowsEventData, List<TableColumn> columnNames) {
        if (columnNames == null) {
            return null;
        }
        List<Map<String, Object>> datas = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        SimpleDateFormat simpleDateFormatGMT = new SimpleDateFormat("d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        try {
            Method method = rowsEventData.getClass().getMethod("getRows");
            Object value = method.invoke(rowsEventData);
            // 新增删除
            if (rowsEventData instanceof DeleteRowsEventData || rowsEventData instanceof WriteRowsEventData) {
                for (Serializable[] row : (List<Serializable[]>) value) {
                    Map<String, Object> item = new HashMap<>();
                    for (int i = 0; i < columnNames.size(); i++) {
                        TableColumn column = columnNames.get(i);

                        if (row[i] != null) {
                            if (row[i] instanceof Date) {
                                try {
                                    Date date = simpleDateFormat.parse(row[i].toString());
                                    item.put(column.getName(), simpleDateFormatGMT.parse(((Date) row[i]).toGMTString()));
                                } catch (Exception e) {
                                    item.put(column.getName(), row[i]);
                                }
                            }

                            if (column.getType().contains("varchar")) {
                                byte[] bt = (byte[]) row[i];
                                item.put(column.getName(), new String(bt, "utf-8"));
                            }
                        } else {
                            item.put(column.getName(), row[i]);
                        }
                    }
                    datas.add(item);
                }
            } else {
                for (Map.Entry<Serializable[], Serializable[]> row : (List<Map.Entry<Serializable[], Serializable[]>>) value) {
                    Map<String, Object> item = new HashMap<>();
                    for (int i = 0; i < columnNames.size(); i++) {
                        TableColumn column = columnNames.get(i);

                        Serializable val = row.getValue()[i];
                        if (val != null) {
                            if (row.getValue()[i] instanceof Date) {
                                try {
                                    Date date = simpleDateFormat.parse(row.getValue()[i].toString());
                                    item.put(column.getName(), simpleDateFormatGMT.parse(((Date) row.getValue()[i]).toGMTString()));
                                } catch (Exception e) {
                                    item.put(column.getName(), row.getValue()[i]);
                                }
                            }

                            // decimal ==》 BigDecimal  float ==》 Float
                            if (column.getType().contains("varchar")) {
                                byte[] bt = (byte[]) val;
                                item.put(column.getName(), new String(bt, "utf-8"));
                            } else if (column.getType().contains("timestamp")) {
                                if (val instanceof Long) {
                                    long time = (long) val;
                                    item.put(column.getName(), new Date(time));
                                }
                            } else {
                                item.put(column.getName(), row.getValue()[i]);
                            }
                        } else {
                            item.put(column.getName(), row.getValue()[i]);
                        }
                    }
                    datas.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datas;
    }

    private static void getColunms(String table) throws SQLException {

        Connection connection = jdbcTemplate.getDataSource().getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tableResultSet = metaData.getTables(null, "test", table, new String[]{"TABLE"});
        try {
            while (tableResultSet.next()) {
                String tableName = tableResultSet.getString("TABLE_NAME");
                ResultSet columnResultSet = metaData.getColumns(null, "test", tableName, null);
                try {
                    while (columnResultSet.next()) {
                        String columnName = columnResultSet.getString("COLUMN_NAME");
                        System.out.println("columnName : " + columnName);
                    }
                } finally {
                    columnResultSet.close();
                }
            }
        } finally {
            tableResultSet.close();
        }
    }
}
