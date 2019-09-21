# start
1st start


内存模型，类加载机制，JVM性能优化，多线程

分布式缓存，消息，负载均衡

sql





RabbitMQ：		并非 分布式，镜像集群模式，每个节点都有全部数据。如果消息数据量非常大，没法玩，所以不是分布式~
	拆分成多个queue，按业务拆分消息，比如当前业务的操作（比如一个订单，1.扣库存，2.生成订单，3.扣钱，4.日志。  放到一个queue里面去，）
	1个业务操作1个queue

Kafka：  纯分布式	。 单线程消费，处理1条消息 几十ms，1秒钟大概处理10+条数据。多线程最高（4核8G，单机，32线程）可以做到上千~ 
	可以保证写入一个partition中的数据一定有顺序
	生成数据在写的时候，指定一个key，这个业务的所有消息都选择当前key
	
	一个partition只能被一个消费者去消费。消费者取出来的时候一定是有序的。
	但是消费者内部一般都是多线程去消费消息。提高吞吐
	再设计一个内存队列，比如按订单id，作为key，那么同一个业务的所有消息都咋一个内存队列中。O了
高可用： 每台机器启动一个broker，多个broker组成，每个broker是集群中的一个节点；创建以后topic，这个topic可以划分多个partition，
		 每个partition可以存在不同的broker上，每个partition就放一部分数据
	HA，replica副本机制。选举一个leader出来。
	
	
	
Redis 单线程为何还支持高并发。  单机redis 的QPS: 上万

1.纯内存操作
2.非阻塞的IO操作，由IO多路复用程序，交给队列转发给 事件处理器 来处理
3.单线程避免了多线程的频繁上下文切换的问题

	NIO 异步
  reactor模式
file event handler 文件事件处理器  --> 单线程，IO多路复用，同时监听多个socket。


一、复制的流程
1.slave node启动，仅保存master node（host,ip）的信息，conf文件中读取到。
2.slave node内部有定数任务，检查是否有master要连接，如果有，则建立socket网络连接。
3.与master口令认证~如果有的话
4.master第一次全量复制，将自己的的所有数据发送给slave。
5.master接受到新数据，异步发送新写的数据到 slave。

二、数据同步的核心机制
	slave第一次连接master的时候的细节。
	master自身不断累加offset，slave也会在自身不断累加offset，slave会不断上报自己的offset到master。
	master和slave知道各种数据的offset，才知道互相之间数据不一致的情况

三、backlog
master log有一个，默认的backlog，默认是1MB
master node在给slave node 复制数据时，也会将数据在backlog 中同步写一份。
backlog主要是用来做全量复制中的 增量复制的。

master run id	类似于pid			使用rdb恢复，run id会变化。
输入命令 info server， 主要是针对master（ip + port + run id）重启的情况，run id不一致会触发全量复制

四、psync			psync runid offset
slave使用psync从master进行复制，master根据自身的情况返回响应信息，可能是fullResync，  也可能是continue增量复制

全量复制和增量复制
master bgsave --> rdb,  repl-timeout， rdb的复制文件时间，默认60s，比如千M网卡，6G的rdb文件就容易超时，如要设置大值。
增量的数据缓存在内存中， client-output-buffer-limit，256MB 64MB 0  :  内存缓存区超过某个值64，或者一次性超过某个值256，或者超过

4-6G大概需要花费1-2min。

quorum 哨兵数量，
min-slaves-to-write  	1		至少有1个slave，数据的同步和复制不能超过10s、
min-slaves-max-lg 		10  	10s内没有收到slave的数据，认为末端不可用，停止master的写。


一致性hash，一个环上n个节点。 当一台宕机，失效流量1/n。
	问题：可能集中在某个hash区间类的值特别多，导致失效后大量数据涌入，造成性能瓶颈。

	虚拟节点： 每个master做了均匀分布的虚拟节点。   原来的环是  :   
	   2	
     /   \
	/     \
   1 ————  3			
	
	    2	
       / \
	 [1]  \   
	 /    [1]
	/       \
   1 ——[1]—— 3

Redis Cluster有固定的16384个hash slot。 对每个key计算CRC16值，然后对16384取模，可以获取key对应的hash slot。
每个master都持有部分slot，比如3个master，那每个就持有 16384/3 个hash slot。
增加一个master，将其他master的hash slot移动部分过去，减少一个master，讲它的移动到其他master。
移动hash slot 的成本非常低。
客户端的api，可以对应指定的数据，让他们走同一个hash slot，通过hash tag 实现。


1、使用Zookeeper。集中式，更新和读取的时效性很好。所有的元数据更新压力集中在一个地方
2、节点之间是有goossip协议通信。【小道流言~】  元数据更新比较分散，更新请求陆陆续续，有延时，降低压力。	
所有节点都持有一份元数据，当前节点变更，发给其他所有节点
	meet， 添加节点
	ping，心跳，信息，元数据交换 
	pong，广播和更新
	fail



缓存雪崩，穿透~
	事前高可用，cluster + 主从。 ehcache可以做个缓存
	事中 hystrix限流+ 降级
	事后，redis需要持久化。尽快恢复缓存集群。


apt-get install 	yum,vim,ssh,net-tools,iputils-ping

docker commit -a "QQ" -m "add utils" imageID redis:v1

Dokcerfile
	FROM redis:v1
	MAINTAINER QQ
	ADD run.sh /run.sh
	RUN chmod 755 /run.sh
	EXPOSE 22
	ENTRYPOINT ["/run.sh"]

docker build -t ubuntu/ssh:v1.1 .


docker save cid -o xxx.tar
docker load -i  xxx.tar

passwd 修改密码


docker run -it --network host --name UUU d46d65671340 /bin/bash

Docker容器里的centos、unbuntu无法使用 systemctl 命令的解决方案		解决方案：/sbin/init	--privilaged=true一定要加上的
System has not been booted with systemd as init system (PID 1). Can't operat
	docker run -it --privileged=true --network bridge --name UUU -p 127.0.0.1:10222:22 9a73f67605c5 /sbin/init
	
docker run -it -p 127.0.0.1:10122:22 d46d65671340 /bin/bash

docker ps  
docker exec -it 775c7c9ee1e1 /bin/bash 
docker run -it --name ubuntu_dev -p 127.0.0.1:10022:22 -p 6379:6379 ubuntu/dev:v1.2 /bin/bash
docker run -it --name ubuntu_dev  ubuntu/dev:v1.2 /bin/bash

export http_proxy=http://Q00492133:QQ900512\!@proxy.huawei.com:8080/


docker commit -a "QQ" -m "add utils" c25bd2b201dd redis:v1.1



docker network create --subnet=172.10.0.0/16 redis-cluster-network
docker run -it --net redis-cluster-network --ip 172.10.0.91 -p 8001:6379 --name redis-test1 ubuntu/dev:v1.2
docker run -it -d --net redis-cluster-network --ip 172.10.0.92 -p 8002:6379 --name redis-test2 ubuntu/dev:v1.2
docker run -it -d --net redis-cluster-network --ip 172.10.0.93 -p 8003:6379 --name redis-test3 ubuntu/dev:v1.2
docker run -it -d --net redis-cluster-network --ip 172.10.0.94 -p 8004:6379 --name redis-test4 ubuntu/dev:v1.2
docker run -it -d --net redis-cluster-network --ip 172.10.0.95 -p 8005:6379 --name redis-test5 ubuntu/dev:v1.2
docker run -it -d --net redis-cluster-network --ip 172.10.0.96 -p 8006:6379 --name redis-test6 ubuntu/dev:v1.2


docker run -it -d --net redis-cluster-network --ip 172.10.0.100 --name ruby11 -i -d redis-ruby:v1

gem install redis  #  --version 3.0.7

./redis-trib.rb  create --replicas 1  172.10.0.91:6379 172.10.0.92:6379 172.10.0.93:6379 172.10.0.94:6379 172.10.0.95:6379 172.10.0.96:6379
redis-cli --cluster create 172.10.0.91:6379 172.10.0.92:6379 172.10.0.93:6379 172.10.0.94:6379 172.10.0.95:6379 172.10.0.96:6379 --cluster-replicas 1


top命令
第一行：
	load average: 0.00, 0.00, 0.00 — load average后面的三个数分别是1分钟、5分钟、15分钟的负载情况。
load average数据是每隔5秒钟检查一次活跃的进程数，然后按特定算法计算出的数值。如果这个数除以逻辑CPU的数量，结果高于5的时候就表明系统在超负荷运转了。

第三行：cpu状态
    0.3% us — 用户空间占用CPU的百分比。
    0.0% sy — 内核空间占用CPU的百分比。
    0.0% ni — 改变过优先级的进程占用CPU的百分比
    99.7% id — 空闲CPU百分比
    0.0% wa — IO等待占用CPU的百分比
    0.0% hi — 硬中断（Hardware IRQ）占用CPU的百分比
    0.0% si — 软中断（Software Interrupts）占用CPU的百分比

监控java线程数：
ps -eLf | grep java | wc -l

监控网络客户连接数：
netstat -n | grep tcp | grep 侦听端口 | wc -l

ls /proc/PID/task | wc -l
在linux中还有一个命令pmap，来输出进程内存的状况，可以用来分析线程堆栈：
pmap PID


1，使用命令top -p <pid> ，显示你的Java进程的内存情况，pid是你的java进程号，比如4977
2，按shift+h --> H，获取每个线程的内存情况 
2.5  1+2 可以直接使用top -Hp pid来查看

3，找到内存和cpu占用最高的线程pid，比如4977 
4，执行 System.out.println(Integer.toHexString(4977));  或者使用printf "%x\n" 4977   得到 0x1371 ,此为线程id的十六进制 
5，执行 jstack 4977|grep -A 10 1371，得到线程堆栈信息中1371这个线程所在行的后面10行 
6，查看对应的堆栈信息找出可能存在问题的代码





Kernel space 是 Linux 内核的运行空间，User space 是用户程序的运行空间。为了安全，它们是隔离的，即使用户的程序崩溃了，内核也不受影响。...
内核空间和用户空间，内核空间是内核代码运行的地方，用户空间是用户程序代码运行的地方。当进程运行在内核空间时就处于内核态，当进程运行在用户空间时就处于用户态。

str = "my string" // 用户空间
x = x + 2
file.write(str) // 切换到内核空间
y = x + 4 // 切换回用户空间




零拷贝：
read(file, tmp_buf, len);
write(socket, tmp_buf, len);


步骤一：读系统调用会导致从【用户模式】到【内核模式】的上下文切换。第一个复制由DMA引擎执行，它读取磁盘中的文件内容并将其存储到内核地址空间缓冲区中。

第二步：将数据从【内核缓冲区】复制到【用户缓冲区】，read系统调用返回。调用的返回导致了从内核返回到用户模式的上下文切换，现在，数据存储在用户地址空间缓冲区中，它可以再次开始向下移动。

第三步:write系统调用导致从用户模式到内核模式的上下文切换，执行第三个复制，将数据再次放入内核地址空间缓冲区中。但是这一次，数据被放入一个不同的缓冲区，这个缓冲区是与套接字相关联的。

第四步：写系统调用返回，创建第四个上下文切换。DMA引擎将数据从内核缓冲区传递到协议engin时，第四个复制发生了独立和异步的情况。你可能会问自己，“你说的独立和异步是什么意思？
”在调用返回之前，数据不是传输的吗？”  实际上，调用返回并不能保证传输;它甚至不能保证传输的开始。它只是意味着以太网驱动程序在其队列中有空闲的描述符并接受了我们的传输数据 ，在我们的之前可能会有很多的数据包在排队。除非驱动/硬件实现了优先级环或队列，否则数据将以先入先出的方式传输。（图1中派生的DMA copy表明了最后一个复制可以被延迟的事实）。

正如您所看到的，大量的数据复制并不是真正需要的。可以消除一些重复，以减少开销并提高性能。 作为一名驱动开发人员，我使用的硬件具有一些非常高级的特性。一些硬件可以完全绕过主存，直接将数据传输到另一个设备上。 该特性消除了系统内存中的复制，这是一件很好的事情，但并不是所有的硬件都支持它。还有一个问题是，磁盘上的数据必须重新打包以供网络使用，这带来了一些复杂的问题。 为了消除开销，我们可以从消除内核和用户缓冲区之间的一些复制开始。
