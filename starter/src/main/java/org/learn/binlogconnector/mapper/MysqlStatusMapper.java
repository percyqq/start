package org.learn.binlogconnector.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.learn.binlogconnector.bean.BinlogPos;
import org.learn.binlogconnector.bean.BinlogPosition;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 */
public interface MysqlStatusMapper {

    @Select("show master status")
    @Results(id = "BinlogPositionDataMap", value = {
            @Result(column = "Position", property = "position"),
            @Result(column = "File", property = "fileName"),
    })
    BinlogPosition getLatestBinlogPosition();

    @Select("show binlog events  in #{fileName} from ${startPos} limit ${offset}, ${count}")
    @Results(id = "BinlogPositionInfoMap", value = {
            @Result(column = "Log_name", property = "logFile"),
            @Result(column = "Pos", property = "position"),
            @Result(column = "Event_type", property = "eventType"),
    })
    List<BinlogPos> getBinlogPos(@Param("fileName") String fileName, @Param("startPos") long start, @Param("offset") int offset, @Param("count") int count);


    // Access denied; you need (at least one of) the REPLICATION SLAVE privilege(s) for this operation
    // limit 2 ： 如果是limit 1 ： 存在某个位点，导致可以查询出来的结果不报错..
    @Select("show binlog events  in #{fileName} from ${startPos} limit 2")
    List<Map<String, Object>> checkBinlog(@Param("fileName") String fileName, @Param("startPos") long start);

}
