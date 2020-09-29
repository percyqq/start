
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

注意先设置 session[一][二]    set autocommit=0;
select @@autocommit;

  [一]                                       [二]                                       [三]
①begin                                      
②                                           begin   
③[ INSERT INTO dog(name) VALUES('二哈');     
    假设此时事务号21]                           
④                                           [ INSERT INTO dog(name) VALUES('柯基');     
                                             假设此时事务号22]  
⑤[SELECT * FROM learn.brand;
此时创建读视图，up_limit_id = 21， 
low_limit_id = 23 活跃事务列表为(21,22)]
⑥                                                                                    [ INSERT INTO dog(name) VALUES('柴柴');     
                                                                                        假设此时事务号23]
⑦                                                                                    [ INSERT INTO dog(name) VALUES('边牧');     
                                                                                        假设此时事务号24]
⑧                                                                                    [ INSERT INTO dog(name) VALUES('金毛');     
                                                                                        假设此时事务号25]  
⑨                                                                                     select * from test; 
                                                                            此时的up_limit_id 为21，low_limit_id 为26，
                                                                            活跃事务列表为（21,22），故21，22在活跃事务列表不可见   
⑩                                          select * from test; 此时low_limit_id为26，up_limit_id 为21，
                                            活跃事务列表是(21,22) 22本事务自身可见。21的在活跃事务列表不可见。
                                            23,24不在活跃事务列表，可见                                   
11 select * from test;  事务内readview不变，
    low_limit_id = 23，up_limit_id = 21，
    活跃事务列表 （21,22）。 
    故21自身可见，22在活跃事务列表不可见。
    >=23的都不可见                                                                                        
                                                                                        


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

