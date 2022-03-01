
https://segmentfault.com/a/1190000012650596
1.⼀个事务快照的创建过程可以概括为：
  a.查看当前所有的未提交并活跃的事务，存储在数组中
  b.选取未提交并活跃的事务中最⼩的XID，记录在快照的xmin中
  c.[选取所有已提交事务中最⼤的XID，加1后记录在xmax中]
2.read view 主要是⽤来做可⻅性判断的, ⽐较普遍的解释便是"本事务不可⻅的当前其他活跃事务", 但正是该解释, 可能会造成⼀节理解上的误区, 
    所以此处提供两个参考, 供给⼤家避开理解误区:
        read view中的`⾼⽔位low_limit_id`可以参考
        https://github.com/zhangyachen/zhangyachen.github.io/issues/68
        https://www.zhihu.com/question/66320138
            其实第1点中加粗部分也是相关⾼⽔位的介绍( 注意进⾏了+1 )
3.另外, 对于read view快照的⽣成时机, 也⾮常关键, 正是因为⽣成时机的不同, 造成了RC,RR两种隔离级别的不同可⻅性;
    ==>在innodb中(默认repeatable read级别), 事务在begin/start transaction之后的[第⼀条select读操作后], 
        会创建⼀个快照(read view), 将当前系统中活跃的其他事务记录记录起来;
    ==>在innodb中(默认repeatable committed级别), 事务中每条select语句都会创建⼀个快照(read view);
参考:https://www.cnblogs.com/digdeep/p/4947694.html
[
    With REPEATABLE READ isolation level, the snapshot is based on the time when the
        first read operation is performed.
    使⽤REPEATABLE READ隔离级别，快照是基于执⾏第⼀个读操作的时间。
!!
    ===>>>   补充一点的是：：  update 是当前读，A事务：快照执行后（比如开启了一个select查询），B事务执行了更新操作，
                            A事务的，当前select 肯定读不到，但是此时 A事务执行的 update语句是可以读到 B事务的操作的!
!!
    --------------------------------------------------------------------------------
    With READ COMMITTED isolation level, the snapshot is reset to the time of each
        consistent read operation.
    使⽤READ COMMITTED隔离级别，快照被重置为每个⼀致的读取操作的时间。
]

最早的事务id： up_limit_id， 最迟的事务id ： low_limit_id
关于low_limit_id，up_limit_id的理解：
[up_limit_id]：[它不是“当前系统的最大活动id”，而应该是当前系统尚未分配的下一个事务id，也就是目前已出现过的事务id的最大值+1。]
    当前已经提交的事务号 + 1，事务号 < up_limit_id ，对于当前Read View都是可见的。
    理解起来就是创建Read View视图的时候，之前已经提交的事务对于该事务肯定是可见的。
    
    
[low_limit_id]：当前最大的事务号 + 1，事务号 >= low_limit_id，对于当前Read View都是不可见的。
    理解起来就是在创建Read View视图之后创建的事务对于该事务肯定是不可见的。
    
RR 下的Read view 举例
从上到下为时间线


输入：     SHOW ENGINE INNODB STATUS;      查看最近一次死锁日志：
set GLOBAL innodb_status_output_locks=ON;
set GLOBAL innodb_status_output=ON;
mysql的锁统计，这个线上不推荐打开打开的话日志会记录得比较多。

[session 1]                                     [session 2]                                 [session 3]
set autocommit=0;
begin;  INSERT INTO `test` (`id`, `a`, `b`) VALUES (64, 64, 64) ON DUPLICATE KEY UPDATE a = 64, b= 64;
begin;  INSERT INTO `test` (`id`, `a`, `b`) VALUES (65, 64, 64) ON DUPLICATE KEY UPDATE a = 64, b= 64;
begin;  INSERT INTO `test` (`id`, `a`, `b`) VALUES (65, 63, 63) ON DUPLICATE KEY UPDATE a = 63, b= 63;
SHOW ENGINE INNODB STATUS;

CREATE TABLE `t2` (
    `id` int NOT NULL,
    `b` int DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `index2` (`b`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3` (
    `id` int NOT NULL,
    `b` varchar(45) COLLATE utf8mb4_general_ci DEFAULT '',
PRIMARY KEY (`id`),
KEY `index2` (`b`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

////
begin;  INSERT INTO `t2` (`id`, `b`) VALUES (64, 64) ON DUPLICATE KEY UPDATE b= 64;
begin;  INSERT INTO `t2` (`id`, `b`) VALUES (65, 63) ON DUPLICATE KEY UPDATE b= 63;
begin;  INSERT INTO `t2` (`id`, `b`) VALUES (65, 62) ON DUPLICATE KEY UPDATE b= 62;

begin;  INSERT INTO `t3` (`id`, `b`) VALUES (1, '') ON DUPLICATE KEY UPDATE b= '';
begin;  INSERT INTO `t3` (`id`, `b`) VALUES (3, '') ON DUPLICATE KEY UPDATE b= '';
begin;  INSERT INTO `t3` (`id`, `b`) VALUES (3, '') ON DUPLICATE KEY UPDATE b= '';
SHOW ENGINE INNODB STATUS;


ps -ef|grep mysql 
cd /                                        
./usr/local/mysql/bin/mysql -u root -p      20210112
use learn;

--> INSERT INTO `learn`.`dog` (`name`) VALUES ('柯基'); 
    UPDATE `learn`.`dog` SET `name` = '柯基135' WHERE (`id` = '18');

设置事务级别  mysql8的语法：      ==> mysql 5.7的语法： set tx_isolation='repeatable-read'; 
[ SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;           
  SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;
  select @@global.transaction_isolation,@@transaction_isolation;     ]

注意先设置 session[一][二]    set autocommit=0;
select @@autocommit;



#######################################################################################################################################
各种加锁研究：
https://mp.weixin.qq.com/s?__biz=MzUxODAzNDg4NQ==&mid=2247497197&idx=1&sn=9f82f73d876636944fb75348ef568c01

CREATE TABLE `test` (
    `id` int NOT NULL,
    `a` int DEFAULT NULL,
    `b` int DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `index_b` (`b`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci

`test` table  data:
id      a       b (index)
0       0       0
4       4       4
8       8       8
16      16      16
32      32      32
[注意字段不用用varchar！不用用varchar！不用用varchar！  varchar对数字的加锁就不是和数字那样预想的一样了    ] 


[对记录加锁时，加锁的基本单位是 next-key lock，它是由记录锁和间隙锁组合而成的，
    next-key lock 是前开后闭区间，而间隙锁是前开后开区间。]
[但是，next-key lock 在一些场景下会退化成记录锁或间隙锁。]

// 默认 autocommit 是1， 需要把session 1的设值为0
============================================== 唯一索引等值查询 ==========================================================================
[session 1]                                     [session 2]                                 [session 3]
set autocommit=0;  select @@autocommit;  begin;
SELECT * FROM test where id = 16 for update;
                                                update test set a = 100 where id = 16;
                                                //阻塞
                                                                                            insert into test values(9, 9, 9);   //OK
分析。 session 1: 
    ① { 记录加锁的基本单位是 next-key lock，因此会话1的加锁范围是(8, 16] }
    ② 但是由于是用唯一索引进行等值查询，且查询的记录存在，所以 next-key lock 退化成 [记录锁]，因此最终加锁的范围是 id = 16 这一行。
所以，会话 2 在修改 id=16 的记录时会被锁住，而会话 3 插入 id=9 的记录可以被正常执行。

接下来，看看记录不存在的情况。
[session 1]                                     [session 2]                                 [session 3]
set autocommit=0;  select @@autocommit;  begin;
SELECT * FROM test where id = 10 for update;
// 记录不存在   
                                                insert into test values(9, 9, 9);
                                                //阻塞
                                                                                            update test set a = 100 where id = 16;  //OK
分析。 session 1:
    ① { 记录加锁的基本单位是 next-key lock，因此会话1的加锁范围是(8, 16] }
    ② 但是由于查询记录不存在，next-key lock 退化成间隙锁，因此最终加锁的范围是 (8,16)。
所以，session 2 要往这个间隙里面插入 id=9 的记录会被锁住，但是 session 3 修改 id =16 是可以正常执行的，因为 id = 16 这条记录并没有加锁。
==> [有趣的是，如果记录9存在，session 2 会提示    Duplicate entry '9' for key 'test.PRIMARY'  ， 而不是锁住！！]
============================================== 唯一索引等值查询 ===========================================================================

********************************************** 唯一索引范围查询 ***************************************************************************
[session 1]                     [session 2]                     [session 3]                     [session 4]
set autocommit=0;  select @@autocommit;  begin;
select * from test where id >= 8 and id < 9 for update;
                                insert into test values(9, 9, 9);
                                //阻塞
                                                                update test set a = 100 where id = 8;
                                                                //阻塞
                                                                                                update test set a = 100 where id = 16;  //OK
分析。 session 1:
    ① 最开始要找的第一行是 id = 8，因此 next-key lock(4,8]， 但是由于 id 是唯一索引，且该记录是存在的，因此会退化成记录锁，也就是只会对 id = 8 这一行加锁；
    ② 由于是范围查找，就会继续往后找存在的记录，也就是会找到 id = 16 这一行停下来，然后加 next-key lock (8, 16]，
    但由于 id = 16 不满足 id < 9，所以会退化成间隙锁，加锁范围变为 (8, 16)。
所以，会话 1 这时候 [主键索引]的锁是记录锁 id=8 和 [间隙锁(8, 16)]。
     会话 2 由于往间隙锁里插入了 id = 9 的记录，所以会被锁住了，而 id = 8 是被加锁的，因此会话 3 的语句也会被阻塞。
     由于 id = 16 并没有加锁，所以会话 4 是可以正常被执行。
********************************************** 唯一索引范围查询 ***************************************************************************
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ 非唯一索引等值查询 $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
当我们用非唯一索引进行等值查询的时候，查询的记录存不存在，加锁的规则也会不同：
    [当查询的记录存在时，除了会加 next-key lock 外，还额外加间隙锁，也就是会加两把锁。]
    [当查询的记录不存在时，只会加 next-key lock，然后会退化为间隙锁，也就是只会加一把锁。]

[session 1]             [session 2]             [session 3]             [session 4]             [session 5]
set autocommit=0;  select @@autocommit;  
begin;  SELECT id FROM test where b = 8 for update;
                        insert into test values(9, 9, 9);
                        //阻塞
                                                insert into test values(5, 5, 5);
                                                //阻塞
                                                                        update test set a = 100 where b = 8;
                                                                        //阻塞
                                                                                                update test set a = 100 where b = 16;
                                                                                                // OK
分析。 session 1:
    ① 先会对普通索引 b 加上 next-key lock，范围是(4,8];
    ② 然后因为是非唯一索引，且查询的记录是存在的，所以还会加上间隙锁，规则是向下遍历到第一个不符合条件的值才能停止，因此间隙锁的范围是(8,16)。
所以，会话1的普通索引 b 上共有两个锁，分别是 next-key lock (4,8] 和间隙锁 (8,16) 。
那么，当会话 2 往间隙锁里插入 id = 9 的记录就会被锁住，而会话 3 和会话 4 是因为更改了 next-key lock 范围里的记录而被锁住的。
然后因为 b = 16 这条记录没有加锁，所以会话 5 是可以正常执行的。

[session 1]                         [session 2]                         [session 3]                    
begin;  SELECT * FROM test where b = 9 for update;
                                    insert into test values(10, 10, 10);
                                    //阻塞
                                                                        update test set a = 100 where b = 16; //OK
输入：SHOW ENGINE INNODB STATUS;查看最近一次死锁日志：
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ 非唯一索引等值查询 $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ 非唯一索引范围查询 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
[session 1]                     [session 2]                     [session 3]                     [session 4]
begin;  SELECT * FROM test where b >= 8 and b < 9 for update;
                                update test set a = 100 where b = 8;     
                                //阻塞
                                                                insert into test values(10, 10, 10);     
                                                                //阻塞
                                                                                                update test set a = 100 where b = 16;   
                                                                                                //阻塞
分析。 session 1:
    ① 最开始要找的第一行是 b = 8，因此 next-key lock(4,8]，但是由于 b 不是唯一索引，并不会退化成记录锁。
    ② 但是由于是范围查找，就会继续往后找存在的记录，也就是会找到 b = 16 这一行停下来，然后加 next-key lock (8, 16]，因为是普通索引查询，所以并不会退化成间隙锁。
所以，会话 1 的普通索引 b 有两个 next-key lock，分别是 (4,8] 和(8, 16]。       全部锁住。
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ 非唯一索引范围查询 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@






===>>>>>>> [注意 update 语句是当前读！！！， 他可以读取到当前事务下的select，读取不到的事务！看第4个场景]
RC是语句级多版本(事务的多条只读语句，创建不同的ReadView，代价更高)，RR是事务级多版本(一个ReadView)；
对于RC隔离就简单多了：
With READ COMMITTED isolation level, each consistent read within a transaction sets and reads its own fresh snapshot.
事务中每一次读取都是以当前的时间点作为判断是否提交的实际点，也即是 reads its own fresh snapshot.


[RR级别下的事务解析！]
====>  [1. RR隔离级别下的一致性读，不是以begin开始的时间点作为snapshot建立时间点，而是以第一条select语句的时间点作为snapshot建立的时间点。]
[sesseion A]                                                [session B]
①： mysql> set tx_isolation='repeatable-read';              mysql> set tx_isolation='repeatable-read';
    Query OK, 0 rows affected (0.00 sec)                    Query OK, 0 rows affected (0.00 sec)
 
②： mysql> begin;
    Query OK, 0 rows affected (0.01 sec)
③：                                                         mysql> select * from t1;
                                                            Empty set (0.00 sec)
                                                            mysql> insert into t1(c1,c2) values(1,1);
                                                            Query OK, 1 row affected (0.01 sec)
④： mysql> select * from t1;
    +----+------+
    | c1 | c2   |
    +----+------+
    |  1 |    1 |
    +----+------+
    1 row in set (0.00 sec)
........................................................................................................................
 
=====> [2. RR隔离级别下的一致性读，是以第一条select语句的执行点作为snapshot建立的时间点的，即使是不同表的select语句。
    这里因为session A在insert之前对 t 表执行了select，所以建立了snapshot，所以后面的select * from t1 不能读取到insert的插入的值。]
[sesseion A]                                                [session B]
①：mysql> set tx_isolation='repeatable-read';               mysql> set tx_isolation='repeatable-read';
                                                            mysql> select * from t1;
                                                            Empty set (0.00 sec)
②：mysql> begin;
   mysql> select * from t;
③：                                                         mysql> insert into t1(c1,c2) values(1,1);
                                                            Query OK, 1 row affected (0.01 sec)
④：mysql> select * from t1;
   Empty set (0.00 sec) 	
........................................................................................................................

=====> [3. session A 的第一条语句，发生在session B的 insert语句提交之前，所以session A中的第二条select还是不能读取到数据。
    因为RR中的一致性读是以事务中第一个select语句执行的时间点作为snapshot建立的时间点的。而此时，session B的insert语句还没有执行，所以读取不到数据。]
[sesseion A]                                                [session B]
①：mysql> set tx_isolation='repeatable-read';                mysql> set tx_isolation='repeatable-read';
                                                             mysql> select * from t1;
                                                             Empty set (0.00 sec)
②：mysql> begin;
③：mysql> select * from t1;                                  mysql> select * from t1;
   Empty set (0.00 sec)                                      Empty set (0.00 sec)
                                                             mysql> insert into t1(c1,c2) values(1,1);   
④：mysql> select * from t1;
   Empty set (0.01 sec)
........................................................................................................................

=====> [4. 本事务中进行修改的数据，即使没有提交，在本事务中的后面也可以读取到。
            update 语句因为进行的是“当前读”，所以它可以修改成功。]
[sesseion A]                                                [session B]
①：mysql> set tx_isolation='repeatable-read';                mysql> set tx_isolation='repeatable-read';
                                                             mysql> select * from t1;
                                                             Empty set (0.00 sec)
②：mysql> begin;     事务需要开始！
   mysql> select * from t1;                                  
   Empty set (0.00 sec) 
③：                                                          mysql> INSERT INTO `learn`.`dog` (`name`) VALUES ('柯基X!'); 
                                                             mysql> select * from t1;
                                                                    ==>  可以查询出   柯基X!
④：mysql> select * from t1;                                  
    Empty set (0.00 sec)                                                                     
⑤：UPDATE `dog` SET `name`='柯基135' WHERE `name`= '柯基X!' ;
   Query OK, 1 row affected (0.00 sec)
   [Rows matched: 1  Changed: 1  Warnings: 0]
    就是这么屌，select读取不到的，我update就读得到！
........................................................................................................................
                                                                    

还是印证了那句话，select是当前读，
    所以 [一] 因为读的太早，啥都读不到， [三]的数据已经提交了！
        [二] 因为读的晚，啥读读得到！！
          
https://github.com/zhangyachen/zhangyachen.github.io/issues/68
  [一]                                       [二]                                       [三]
① [begin;]                                      
②                                           [begin;]   
③[ INSERT INTO dog(name) VALUES('二哈');     
    假设此时事务号21]                           
④                                           [ INSERT INTO dog(name) VALUES('柯基');     
                                             假设此时事务号22]  
⑤[SELECT * FROM learn.brand;
此时创建读视图，up_limit_id = 21， 
low_limit_id = 23 活跃事务列表为(21,22)]
    ==>  只 看得到  '二哈'
⑥                                                                                    [ INSERT INTO dog(name) VALUES('柴柴');     
                                                                                        假设此时事务号23]
⑦                                                                                    [ INSERT INTO dog(name) VALUES('边牧');     
                                                                                        假设此时事务号24]
⑧                                                                                    [ INSERT INTO dog(name) VALUES('金毛');     
                                                                                        假设此时事务号25]  
⑨                                                                                     select * from test; 
                                                                                    此时的up_limit_id 为21，low_limit_id 为26，
                                                                                    活跃事务列表为（21,22），故21，22在活跃事务列表不可见   
⑩                                          select * from test; 
                                            [==> '柯基' '柴柴' '边牧' '金毛' 都看得到！]
                                            此时low_limit_id为26，up_limit_id 为21，
                                            活跃事务列表是(21,22) 22本事务自身可见。
                                            21的在活跃事务列表不可见。
                                            23,24不在活跃事务列表，可见                                   
11 select * from test;  
    [只 看得到  '二哈']
    事务内readview不变，
    low_limit_id = 23，up_limit_id = 21，
    活跃事务列表 （21,22）。 
    故21自身可见，22在活跃事务列表不可见。
    >=23的都不可见                                                                                        
    [有趣的事：产生 被锁！，无法更新...  ===> 间隙锁？
        被锁失败！ ： UPDATE `learn`.`dog` SET `name` = '柯基135' WHERE `name` = '边牧';
        更新成功！ ： UPDATE `learn`.`dog` SET `name` = '柯基135' WHERE `id` = xxx   
    ]                                                                       xxx 即是：[三]中的id。。           

RR级别下会出现：
库存死锁的场景，那张图 update scm_dispatch_org_inventory set qty=?, lock_qty=? where id=? and [server_update_time=?]
这也就解释了死锁的场景：server_update_time是范围，不像id那样精准。不过如果是趋势递增的id，也会死锁。
1. 事务1 begin， 
        2. 事务2 begin， 
3. 事务1 insert 
        4. 事务2 insert
5. 事务1 select       
        6. 事务3 begin， insert  commit！
        7. 事务2 select
8. 事务1  去update


INSERT INTO learn.brand(id,name,status,createTime) VALUES ('2', '102度健康火锅', '0', '2013-08-02 15:06:17');
INSERT INTO learn.brand(id,name,status,createTime) VALUES ('3', '1920 Restaurant and Bar', '0', '2016-05-17 10:32:06');
INSERT INTO learn.brand(id,name,status,createTime) VALUES ('4', '2108 Restaurant&Bar', '0', '2016-05-17 10:32:06');








https://www.jianshu.com/p/d75fcdeb07a3

#不同隔离级别带来的数据操作问题：
1.脏读：两个事务，t1事务可以读取到t2事务正在做更改的数据的中间状态(t2事务执行过程中)，而这个数据的更改有可能不会被持久化(commit)，
    而是rollback，导致t1在同一事务内的两次读取同一行数据得到结果不同。
2.不可重复读：t1事务在整个事务执行过程中读取某一条记录多次，发现读取的此条记录不是每次都一样。
3.幻读：t1事务在整个事务执行过程中读取某一范围内的数据，在第二次读取时发现多了几行或者少了几行。

#数据库中的几种隔离级别
1.read uncommited--读未提交
    该隔离级别指即使一个事务的更新语句没有提交,但是别的事务可以读到这个改变，几种异常情况都可能出现。极易出错，没有安全性可言，基本不会使用。
2.read committed --读已提交
    该隔离级别指一个事务只能看到其他事务的已经提交的更新，看不到未提交的更新，消除了脏读和第一类丢失更新，
    这是大多数数据库的默认隔离级别，如Oracle,Sqlserver。
3.repeatable read --可重复读
    该隔离级别指一个事务中进行两次或多次同样的对于[数据内容]的查询，得到的结果是一样的，但不保证对于[数据条数]的查询是一样的，
    只要存在读改行数据就禁止写，消除了不可重复读和第二类更新丢失，这是Mysql数据库的默认隔离级别。
4.serializable --序列化读
    意思是说这个事务执行的时候不允许别的事务并发写操作的执行.完全串行化的读，只要存在读就禁止写,但可以同时读，消除了幻读。
    这是事务隔离的最高级别，虽然最安全最省心，但是效率太低，一般不会用。

#数据库中的锁:
1.共享锁（Share locks简记为S锁）：也称读锁，事务A对对象T加s锁，其他事务也只能对T加S，多个事务可以同时读，但不能有写操作，直到A释放S锁。
2.排它锁（Exclusivelocks简记为X锁）：也称写锁，事务A对对象T加X锁以后，其他事务不能对T加任何锁，只有事务A可以读写对象T直到A释放X锁。
3.更新锁（简记为U锁）：用来预定要对此对象施加X锁，它允许其他事务读，但不允许再施加U锁或X锁；
    当被读取的对象将要被更新时，则升级为X锁，主要是用来防止死锁的。
    因为使用共享锁时，修改数据的操作分为两步，首先获得一个共享锁，读取数据，然后将共享锁升级为排它锁，然后再执行修改操作。
    这样如果同时有两个或多个事务同时对一个对象申请了共享锁，在修改数据的时候，这些事务都要将共享锁升级为排它锁。
    这些事务都不会释放共享锁而是一直等待对方释放，这样就造成了死锁。
    如果一个数据在修改前直接申请更新锁，在数据修改的时候再升级为排它锁，就可以避免死锁。



####[实操]###
=========================登录========================================
cd /
.usr/local/mysql/bin/mysql -h 127.0.0.1 -uroot -pQQ6*
set autocommit=0;
select @@autocommit;

-- serializable;  read uncommitted;	repeatable read;
set session transaction isolation level read uncommitted;
select @@global.transaction_isolation,@@transaction_isolation;

use learn;
update learn.inventory 	set lock_qty = 1.35 where id = 314044536575331328;
SELECT * FROM learn.inventory where id = 314044536575331328;

-- begin;rollback;commit;
====================================================================

[开第二个session]
set autocommit=0;
set session transaction isolation level read uncommitted;
update learn.inventory 	set lock_qty = 1.35 where id = 314044536575331328;
SELECT * FROM learn.inventory where id = 314044536575331328;


[开第三个session]
use sys;
show tables like '%lock%';
可以看到有4个锁表  
            | innodb_lock_waits         |
            | schema_table_lock_waits   |
            | x$innodb_lock_waits       |
            | x$schema_table_lock_waits |


SELECT * FROM sys.`innodb_lock_waits`;
关注列： [blocking_lock_mode] : X,REC_NOT_GAP

查看当前被锁的语句
SELECT * FROM performance_schema.events_statements_history WHERE thread_id IN(
	SELECT b.`THREAD_ID` FROM sys.`innodb_lock_waits` AS a , performance_schema.threads AS b
	WHERE a.waiting_pid = b.`PROCESSLIST_ID`
)
ORDER BY timer_start ASC;


------------------------------------------------------------------------
[可以得出结论，read uncommitted 隔离级别下，写操作是有锁的，而且是 X 排他锁，]
    读取UNCOMMITTED事务也不被排他锁(排他锁将阻止当前事务读取已被修改但未被其他事务提交的行)阻止
其实想想也对，应为排它锁对任何其他的事务开始之前申请的排它锁，共享锁都不兼容。但是如果我读不申请锁，就不会产生上述问题了呀。

所以最终结论是：read uncommitted 读不加锁，写加排他锁，并到事务结束之后释放。

