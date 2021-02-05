package org.learn.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import static com.alibaba.otter.canal.protocol.CanalEntry.*;

//@Component
public class CannalClient implements InitializingBean {

    private final static int BATCH_SIZE = 1000;

    /**
     *  ==>  [conf/canal.properties] 在demo场景一般不会修改
     *  ==>  [conf/canal.properties] 这里是需要连接canal的server端！ 注意开放对应的端口！
     *  sudo ufw allow 11111
     *
     *  而   [conf/example/instance.properties]  里面才是配置对应的数据库连接地址的！
     *       监听对应的mysql-binlog！
     *
     *  坑：
     *  https://blog.csdn.net/weixin_44188501/article/details/107392089
     * */
    public static void main(String[] args) {

        try {
            new CannalClient().afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //注意检查ip变化！
    private static final String IP = "10.242.1.40";

    @Override
    public void afterPropertiesSet() throws Exception {

//        Class.forName("com.mysql.jdbc.Driver");
//        java.sql.Connection connection = java.sql.DriverManager.getConnection(
//                "jdbc:mysql://10.242.0.155:13306/test?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC",
//                "bkjk", "test@123");
//
//        String sql = "select * from collect_task_progress where id = ?";
//        java.sql.PreparedStatement preparedStatement = connection.prepareStatement(sql);
//        preparedStatement.setInt(1, 1);
//
//        java.sql.ResultSet resultSet = preparedStatement.executeQuery();
//        while (resultSet.next()) {
//            System.out.println(resultSet.getInt("id") + " " + resultSet.getString("name"));
//        }


        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(IP, 11111), "example", "", "");

        //connector = CanalConnectors.newSingleConnector(
        //        new InetSocketAddress("127.0.0.1", 13306), "test", "bkjk", "test@123");

        try {
            //打开连接
            System.out.println(2222);
            connector.connect();
            System.out.println(111);
            //订阅数据库表,全部表
            connector.subscribe(".*\\..*");
            //回滚到未进行ack的地方，下次fetch的时候，可以从最后一个没有ack的地方开始拿
            connector.rollback();
            while (true) {
                // 获取指定数量的数据
                Message message = connector.getWithoutAck(BATCH_SIZE);
                //获取批量ID
                long batchId = message.getId();
                //获取批量的数量
                int size = message.getEntries().size();
                //如果没有数据
                if (batchId == -1 || size == 0) {
                    try {
                        //线程休眠2秒
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //如果有数据,处理数据
                    printEntry(message.getEntries());
                }
                //进行 batch id 的确认。确认之后，小于等于此 batchId 的 Message 都会被确认。
                connector.ack(batchId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connector.disconnect();
        }
    }

    /**
     * 打印canal server解析binlog获得的实体类信息
     */
    private static void printEntry(List<Entry> entrys) {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                //开启/关闭事务的实体类型，跳过
                continue;
            }
            //RowChange对象，包含了一行数据变化的所有特征
            //比如isDdl 是否是ddl变更操作 sql 具体的ddl sql beforeColumns afterColumns 变更前后的数据字段等等
            CanalEntry.RowChange rowChage;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
            }
            //获取操作类型：insert/update/delete类型
            EventType eventType = rowChage.getEventType();
            //打印Header信息
            System.out.println(String.format("================》; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));
            //判断是否是DDL语句
            if (rowChage.getIsDdl()) {
                System.out.println("================》;isDdl: true,sql:" + rowChage.getSql());
            }
            //获取RowChange对象里的每一行数据，打印出来
            for (RowData rowData : rowChage.getRowDatasList()) {
                //如果是删除语句
                if (eventType == EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                    //如果是新增语句
                } else if (eventType == EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                    //如果是更新的语句
                } else {
                    //变更前的数据
                    System.out.println("------->; before");
                    printColumn(rowData.getBeforeColumnsList());
                    //变更后的数据
                    System.out.println("------->; after");
                    printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private static void printColumn(List<Column> columns) {
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}