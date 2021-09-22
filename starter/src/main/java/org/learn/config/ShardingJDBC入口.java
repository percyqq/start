package org.learn.config;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.ShardingPreparedStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 */
public class ShardingJDBC入口 {


    PreparedStatement ps;

    {
        try {
            ps = new ShardingPreparedStatement(
                    new ShardingConnection(null, null, null),
                    ""
            );

            ps.execute("select * from A");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }


}
