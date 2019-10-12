https://github.com/crossoverJie/JCSprout  
    Java 核心知识库  


https://ifeve.com/overview/  
    Java NIO系列教程（一） Java NIO 概述   


https://tech.meituan.com/2019/02/14/talk-about-java-magic-class-unsafe.html  
    Java魔法类：Unsafe应用解析  

https://tech.meituan.com/2019/02/28/java-dynamic-trace.html  
    Java动态追踪技术探究  
    
https://tech.meituan.com/2019/09/05/java-bytecode-enhancement.html   
    字节码增强技术探索  
    
    
https://tech.meituan.com/2014/08/20/innodb-lock.html  
    Innodb中的事务隔离级别和锁的关系  
    
https://mp.weixin.qq.com/s/V1hGa6D9aGrP6PiCWEmc0w  
    架构师之路18年精选100篇  

https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651960945&idx=1&sn=d08f33c5f317fee8956252da8e0236b6&chksm=bd2d03ad8a5a8abb0370b826b7384a4095a5ed36238f0911d102b0ceee8e5d2fbe3bc80c56d9&scene=21#wechat_redirect
    架构师之路17年精选80篇      
    

https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651959942&idx=1&sn=e9d3fe111b8a1d44335f798bbb6b9eea&chksm=bd2d075a8a5a8e4cad985b847778aa83056e22931767bb835132c04571b66d5434020fd4147f&scene=21#wechat_redirect  
    业界难题-“跨库分页”的四种方案  

四、终极武器-二次查询法
  为了方便举例，假设一页只有5条数据，查询第200页的SQL语句为select * from T order by time offset 1000 limit 5;
* 步骤一：查询改写  

将select * from T order by time offset 1000 limit 5  
改写为select * from T order by time offset 500 limit 5  
并投递给所有的分库，注意，这个offset的500，来自于全局offset的总偏移量1000，除以水平切分数据库个数2。  

如果是3个分库，则可以改写为select * from T order by time offset 333 limit 5  
假设这三个分库返回的数据(time, uid)如下：  
每个分库都是返回的按照time排序的一页数据。  
    第一个库，5条数据的time最小值是1487501123  
    第二个库，5条数据的time最小值是1487501133  
    第三个库，5条数据的time最小值是1487501143  

* 步骤二：找到所返回3页全部数据的最小值
    第一个库，5条数据的time最小值是1487501123
    第二个库，5条数据的time最小值是1487501133
    第三个库，5条数据的time最小值是1487501143




https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651960212&idx=1&sn=ab4c52ab0309f7380f7e0207fa357128&chksm=bd2d06488a5a8f5e3b7c9de0cc5936818bd9a6ed4058679ae8d819175e0693c6fbd9cdea0c87&scene=21#wechat_redirect  
    单KEY业务，数据库水平切分架构实践 | 架构师之路  

https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651959883&idx=1&sn=e7df8510c7096a5b069e0f12eaaca010&chksm=bd2d07978a5a8e815c2ae41b16b6b4c579923502fb919008a22bb108a1e920109f25387f8903&scene=21#wechat_redirect
    数据库秒级平滑扩容架构方案  
    
https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651959992&idx=1&sn=eb2fbd7d7922db42a593c304e50a65b7&chksm=bd2d07648a5a8e72d489022ec6006274d7e43ab48449b255d5661658c2af8e9221977a9609ed&scene=21#wechat_redirect  
    100亿数据平滑数据迁移,不影响服务
    
    
https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651961620&idx=1&sn=d858c302799cad451656129885214767&chksm=bd2d0cc88a5a85de11ed376570f78a22954e88aad06f0138b3fbfb7be1968699421ac0b99889&scene=21#wechat_redirect
    关于MySQL内核，一定要知道的！  
    
https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651961455&idx=1&sn=4c26a836cff889ff749a1756df010e0e&chksm=bd2d0db38a5a84a53db91e97c7be6295185abffa5d7d1e88fd6b8e1abb3716ee9748b88858e2&scene=21#wechat_redirect      
    1.插入InnoDB自增列，居然是表锁？  
    
https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651961461&idx=1&sn=b73293c71d8718256e162be6240797ef&chksm=bd2d0da98a5a84bfe23f0327694dbda2f96677aa91fcfc1c8a5b96c8a6701bccf2995725899a&scene=21#wechat_redirect    
    2.InnoDB并发插入，居然使用意向锁？    
    
https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651961471&idx=1&sn=da257b4f77ac464d5119b915b409ba9c&chksm=bd2d0da38a5a84b5fc1417667fe123f2fbd2d7610b89ace8e97e3b9f28b794ad147c1290ceea&scene=21#wechat_redirect      
    InnoDB，select为啥会阻塞insert？  
    
https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651961508&idx=1&sn=9f31a95e5b8ec16fa0edc7de6087d2a1&chksm=bd2d0d788a5a846e3bf16d300fb9723047bd109fd22682c39bdf7ed4e77b167e333460f6987c&scene=21#wechat_redirect      
    别废话，各种SQL到底加了什么锁？  
    
 https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651961444&idx=1&sn=830a93eb74ca484cbcedb06e485f611e&chksm=bd2d0db88a5a84ae5865cd05f8c7899153d16ec7e7976f06033f4fbfbecc2fdee6e8b89bb17b&scene=21#wechat_redirect     
    InnoDB并发如此高，原因竟然在这？  
    








https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651960245&idx=1&sn=5cef3d8ca6a3e6e94f61e0edaf985d11&chksm=bd2d06698a5a8f7fc89056af619b9b7e79b158bceb91bdeb776475bc686721e36fb925904a67&scene=21#wechat_redirect  
    分布式ID生成器 | 架构师之路
