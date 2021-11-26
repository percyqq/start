package org.learn.binlogconnector.util;

import com.github.shyiko.mysql.binlog.event.deserialization.json.JsonBinary;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.learn.binlogconnector.bean.TableColumn;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Slf4j
public class DataDeserializerUtil {

    private static final int timeOffset = TimeZone.getDefault().getRawOffset();

    private static final FastDateFormat datetimeFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    // 检查下 数据列的 定义，这里到底怎么取值
    public static List<Map<Map<String, Object>, Map<String, Object>>> deserializeMulti(List<TableColumn> columnNames,
                                                                                       List<Map.Entry<Serializable[], Serializable[]>> value) {
        List<Map<Map<String, Object>, Map<String, Object>>> datas = new ArrayList<>();

        for (Map.Entry<Serializable[], Serializable[]> row : value) {
            List<Serializable[]> oldValue = new ArrayList<>(1);
            List<Serializable[]> newValue = new ArrayList<>(1);
            Serializable[] oldData = row.getKey();
            Serializable[] newData = row.getValue();
            oldValue.add(oldData);
            newValue.add(newData);

            List<Map<String, Object>> oldRet = deserialize(columnNames, oldValue);
            List<Map<String, Object>> newRet = deserialize(columnNames, newValue);

            Map<Map<String, Object>, Map<String, Object>> data = new HashMap<>();
            data.put(oldRet.get(0), newRet.get(0));
            datas.add(data);
        }

        return datas;
    }


    /**
     * 由反序列化的类{@link com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer}
     * 的配置决定具体的类型值，
     * 1. Date/Time ==> Long,
     * 2. varchar ==> byte[]
     */
    public static List<Map<String, Object>> deserialize(List<TableColumn> columnNames, List<Serializable[]> value) {
        int size = columnNames.size();
        List<Map<String, Object>> datas = new ArrayList<>(value.size());
        for (Serializable[] row : value) {
            //一条数据以Map形式存储
            Map<String, Object> item = new LinkedHashMap<>();
            for (int i = 0; i < size; i++) {
                TableColumn column = columnNames.get(i);
                String fieldType = column.getFieldType();
                Object val = row[i];
                if (val != null) {
//                    if (fieldType.contains("varchar")) {
//                        item.put(column.getFieldName(), row[i]);
//                    } else if (fieldType.contains("timestamp")) {
//                        Serializable val = row[i];
////                        if (val instanceof Long) {
////                            long time = (long) val;
////                            item.put(column.getFieldName(), new Date(time));
////                        }
//                        item.put(column.getFieldName(), val);
//                    }
                    if (val instanceof byte[] && fieldType.contains("text")) {
                        // *text 类型 是byte[] 返回的
                        String data = new String((byte[]) val);
                        item.put(column.getFieldName(), data);
                    } else if (val instanceof byte[] && fieldType.equalsIgnoreCase("json")) {
                        // json 格式是特有的格式，需要单独解析...
                        try {
                            String w = JsonBinary.parseAsString((byte[]) val);
                            item.put(column.getFieldName(), w);
                        } catch (IOException e) {
                            log.error("BINLOG deserialize [json] error:{},{},{}", column, val, row, e);
                            item.put(column.getFieldName(), val);
                        }
                    } else if (val instanceof Long && (fieldType.contains("date") || fieldType.equals("timestamp"))) {
                        //datetime 有时差， timestamp 没有
                        long time = (long) val;
                        if (time > timeOffset && fieldType.equals("datetime")) {
                            //datetime类型
                            time -= timeOffset;
                        }
                        item.put(column.getFieldName(), datetimeFormat.format(time));
                    } else {
                        item.put(column.getFieldName(), val);
                    }
                } else {
                    item.put(column.getFieldName(), null);
                }
            }
            datas.add(item);
        }

        return datas;
    }

}
