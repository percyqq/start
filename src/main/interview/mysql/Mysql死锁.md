
update learn.inventory 	set lock_qty = 2.46 where id = 314044536575331328
and server_update_time = ?

分析： id是趋势递增的，会不会有间隙锁，导致了某一个区域锁住，从而形成了死锁？

ps -ef|grep mysql 
cd /                                        QQ900512
./usr/local/mysql/bin/mysql -u root -p 

use learn;
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED; 


set autocommit=0;
select @@autocommit;      


mysql线上insert死锁问题的研究
https://www.cnblogs.com/sunss/p/3166550.html
https://blog.csdn.net/ignorewho/article/details/86547130?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-5.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-5.control
https://www.lagou.com/lgeduarticle/82810.html

https://segmentfault.com/a/1190000038556324
https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html
https://dev.mysql.com/doc/refman/8.0/en/innodb-deadlock-example.html












