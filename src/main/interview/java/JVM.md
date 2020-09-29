从 Java 8 开始，JDK 使用 invokedynamic 及 VM Anonymous Class 结合来 实现 Java 语言层面上的 Lambda 表达式。

？？记得遇到过 生成的匿名类过多，服务器的内存泄露问题。

JVM 回收线程的个数

查看安装的java路径：
/usr/libexec/java_home -V

wms项目的参考
-server -Xms4096M -Xmx6144M -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=256M -Xss256k -XX:MaxTenuringThreshold=15 
    -Xnoclassgc -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection 
    -XX:CMSInitiatingOccupancyFraction=65 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ExplicitGCInvokesConcurrent 
    -XX:CMSFullGCsBeforeCompaction=0 -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC 
    -Xloggc:logs/JVMGC.log -XX:+CMSClassUnloadingEnabled 
    -Djava.awt.headless=true -Duser.timezone=GMT+08 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap


-Xmx3550m：设置JVM最大堆内存为3550M。
-Xms3550m：设置JVM初始堆内存为3550M。此值可以设置与-Xmx相同，以避免每次垃圾回收完成后JVM重新分配内存。

-Xmn2g：设置年轻代大小为2G。在整个堆内存大小确定的情况下，增大年轻代将会减小年老代，反之亦然。
    此值关系到JVM垃圾回收，对系统性能影响较大，官方推荐配置为整个堆大小的3/8。

-XX:NewSize=1024m：设置年轻代初始值为1024M。
-XX:MaxNewSize=1024m：设置年轻代最大值为1024M。
-XX:PermSize=256m：设置持久代初始值为256M。
-XX:MaxPermSize=256m：设置持久代最大值为256M。

-XX:NewRatio=4：设置年轻代（包括1个Eden和2个Survivor区）与年老代的比值。表示年轻代比年老代为1:4。
-XX:SurvivorRatio=4：设置年轻代中Eden区与Survivor区的比值。
    表示2个Survivor区（JVM堆内存年轻代中默认有2个大小相等的Survivor区）与1个Eden区的比值为2:4，
    即1个Survivor区占整个年轻代大小的1/6。
-XX:MaxTenuringThreshold=7：表示一个对象如果在Survivor区（救助空间）移动了7次还没有被垃圾回收就进入年老代。
    如果设置为0的话，则年轻代对象不经过Survivor区，直接进入年老代，对于需要大量常驻内存的应用，这样做可以提高效率。
    如果将此值设置为一个较大值，则年轻代对象会在Survivor区进行多次复制，这样可以增加对象在年轻代存活时间，
    增加对象在年轻代被垃圾回收的概率，减少Full GC的频率，这样做可以在某种程度上提高服务稳定性。


空间分配担保  
    在发生 Minor GC 之前，虚拟机会先检查老年代最大可用的连续空间是否大于新生代所有对象总空间，
    如果这个条件成立，那么 Minor GC 可以确保是安全的。
    如果不成立，则虚拟机会查看 HandlePromotionFailure 设置值是否允许担保失败。
    如果允许，那么会继续检查老年代最大可能连续空间是否大于历次晋升到老年代对象的平均大小，
    如果大于，将尝试着进行一次 Minor GC，尽管这个 Minor GC 是有风险的；
    如果小于，或 HandlePromotionFailure 设置不允许冒险，那这次也要改为进行一次 Full GC。


在内存担保机制下，无法安置的对象会直接进到老年代，以下几种情况也会进入老年代。
大对象
    大对象指需要大量连续内存空间的对象，这部分对象不管是不是“朝生夕死”，都会直接进到老年代。
    这样做主要是为了避免在 Eden 区及2个 Survivor 区之间发生大量的内存复制。
    当你的系统有非常多“朝生夕死”的大对象时，得注意了。

长期存活对象
    虚拟机给每个对象定义了一个对象年龄（Age）计数器。正常情况下对象会不断的在 Survivor 的 From 区与 To 区之间移动，
        对象在 Survivor 区中每经历一次 Minor GC，年龄就增加1岁。当年龄增加到15岁时，这时候就会被转移到老年代。
        当然，这里的15，JVM 也支持进行特殊设置。

动态对象年龄
    虚拟机并不重视要求对象年龄必须到15岁，才会放入老年区，如果 Survivor 空间中相同年龄所有对象大小的总合大于 
        Survivor 空间的一半，年龄大于等于该年龄的对象就可以直接进去老年区，无需等你“成年”。

这其实有点类似于负载均衡，轮询是负载均衡的一种，保证每台机器都分得同样的请求。
    看似很均衡，但每台机的硬件不通，健康状况不同，我们还可以基于每台机接受的请求数，或响应时间等，来调整我们的负载均衡算法。


[VarHandle 在jdk9开始替代料Unsafe]

Unsafe的使用地方
1、数组  ==> 返回数组元素内存大小，数组首元素便宜地址
2.内存屏障  ==>  禁止load， store重排序

4.线程调度 ==> 释放、获取锁， 线程挂起、恢复
5.内存操作 ==> 对外内存
6.CAS
7.class相关  动态创建类，匿名，  获取field偏移地址
8.对象操作

https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/gc01/index.html    
	Java Garbage Collection Basics  
	
https://blog.csdn.net/m0_37568814/article/details/88670280  
JVM GC日志分析  

https://www.cnblogs.com/1024Community/p/honery.html  
	扒一扒JVM的垃圾回收机制   


https://www.cnblogs.com/hexinwei1/p/9556259.html  
JVM命令-java服务器故障排查  

https://www.cnblogs.com/yjd_hycf_space/p/7755633.html  
 jstat命令查看jvm的GC情况  

https://www.cnblogs.com/redcreen/archive/2011/05/04/2037057.html  
JVM系列三:JVM参数设置、分析  

https://www.cnblogs.com/aspirant/p/8662690.html    
	JVM的垃圾回收机制 总结(垃圾收集、回收算法、垃圾回收器)
  
https://ifeve.com/case-of-hashmap-in-concurrency/  
        并发环境下HashMap引起的full gc排查
  
	日志查看：cat stack.log | grep atw | sort | uniq -c | sort -nr | head -10  
https://mp.weixin.qq.com/s/iN_zwgpOudlYNem4jQu1ew  
		类加载器的GC排查
 
https://tech.meituan.com/2016/09/23/g1.html  
	Java Hotspot G1 GC的一些关键技术 

https://tech.meituan.com/2017/12/29/jvm-optimize.html  
	从实际案例聊聊Java应用的GC优化


https://hllvm-group.iteye.com/group/wiki/2859-JVM
方法区即后文提到的永久代，很多人认为永久代是没有GC的，《Java虚拟机规范》中确实说过可以不要求虚拟机在这区实现GC，
    而且这区GC的“性价比”一般比较低：在堆中，尤其是在新生代，常规应用进行一次GC可以一般可以回收70%~95%的空间，
    而永久代的GC效率远小于此。虽然VM Spec不要求，
    但当前生产中的商业JVM都有实现永久代的GC，主要回收两部分内容：废弃常量与无用类。
        这两点回收思想与Java堆中的对象回收很类似，都是搜索是否存在引用，常量的相对很简单，与对象类似的判定即可。
        而类的回收则比较苛刻，需要满足下面3个条件：
        　　1.该类所有的实例都已经被GC，也就是JVM中不存在该Class的任何实例。
        　　2.加载该类的ClassLoader已经被GC。
        　　3.该类对应的java.lang.Class 对象没有在任何地方被引用，如不能在任何地方通过反射访问该类的方法。

　　是否对类进行回收可使用-XX:+ClassUnloading参数进行控制，还可以使用-verbose:class或者-XX:+TraceClassLoading、
        -XX:+TraceClassUnLoading查看类加载、卸载信息。

[在大量使用反射、动态代理、CGLib等bytecode框架、动态生成JSP以及OSGi这类频繁自定义ClassLoader的场景都需要
    JVM具备类卸载的支持以保证永久代不会溢出。]


Java8的GC情况是 
    -XX:+UseParallelGC，即Parallel Scavenge（新生代） + Parallel Old（老生代）
cmd命令行查看Java8的GC：       /Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home/bin
    java -XX:+PrintCommandLineFlags -version
    
    -XX:InitialHeapSize=134217728 -XX:MaxHeapSize=2147483648 -XX:+PrintCommandLineFlags -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC 
    java version "1.8.0_231"
    Java(TM) SE Runtime Environment (build 1.8.0_231-b11)
    Java HotSpot(TM) 64-Bit Server VM (build 25.231-b11, mixed mode)
    
cmd命令行查看Java8的GC详细情况：   /Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home/bin
    java -XX:+PrintGCDetails -version  
    
    java version "1.8.0_231"
    Java(TM) SE Runtime Environment (build 1.8.0_231-b11)
    Java HotSpot(TM) 64-Bit Server VM (build 25.231-b11, mixed mode)
    Heap
     PSYoungGen      total 38400K, used 1331K [0x0000000795580000, 0x0000000798000000, 0x00000007c0000000)
      eden space 33280K, 4% used [0x0000000795580000,0x00000007956cce48,0x0000000797600000)
      from space 5120K, 0% used [0x0000000797b00000,0x0000000797b00000,0x0000000798000000)
      to   space 5120K, 0% used [0x0000000797600000,0x0000000797600000,0x0000000797b00000)
     ParOldGen       total 87552K, used 0K [0x0000000740000000, 0x0000000745580000, 0x0000000795580000)
      object space 87552K, 0% used [0x0000000740000000,0x0000000740000000,0x0000000745580000)
     Metaspace       used 2244K, capacity 4480K, committed 4480K, reserved 1056768K
      class space    used 243K, capacity 384K, committed 384K, reserved 1048576K
  

https://hllvm-group.iteye.com/group/wiki/2859-JVM
1.Serial收集器
　　单线程收集器，收集时会暂停所有工作线程（我们将这件事情称之为Stop The World，下称STW），使用复制收集算法，
    虚拟机运行在Client模式时的默认新生代收集器。

2.ParNew收集器
　　ParNew收集器就是Serial的多线程版本，除了使用多条收集线程外，其余行为包括算法、STW、对象分配规则、回收策略等
    都与Serial收集器一摸一样。对应的这种收集器是虚拟机运行在Server模式的默认新生代收集器，在单CPU的环境中，
    ParNew收集器并不会比Serial收集器有更好的效果。

3.Parallel Scavenge收集器
　　Parallel Scavenge收集器（下称PS收集器）也是一个多线程收集器，也是使用复制算法，但它的对象分配规则与回收策略都与ParNew收集器有所不同，它是以吞吐量最大化（即GC时间占总运行时间最小）为目标的收集器实现，它允许较长时间的STW换取总吞吐量最大化。

4.Serial Old收集器
　　Serial Old是单线程收集器，使用标记－整理算法，是老年代的收集器，上面三种都是使用在新生代收集器。

5.Parallel Old收集器
　　老年代版本吞吐量优先收集器，使用多线程和标记－整理算法，JVM 1.6提供，在此之前，新生代使用了PS收集器的话，
    老年代除Serial Old外别无选择，因为PS无法与CMS收集器配合工作。

6.CMS（Concurrent Mark Sweep）收集器
　　CMS是一种以最短停顿时间为目标的收集器，使用CMS并不能达到GC效率最高（总体GC时间最小），但它能尽可能降低GC时服务的停顿时间，
    这一点对于实时或者高交互性应用（譬如证券交易）来说至关重要，这类应用对于长时间STW一般是不可容忍的。
    CMS收集器使用的是标记－清除算法，也就是说它在运行期间会产生空间碎片，
    所以虚拟机提供了参数开启CMS收集结束后再进行一次内存压缩。

	
关于上文中提到晋升年龄阈值为2，很多同学有疑问，为什么设置了MaxTenuringThreshold=15，
    对象仍然仅经历2次Minor GC，就晋升到老年代？这里涉及到“动态年龄计算”的概念。

动态年龄计算：
    Hotspot遍历所有对象时，按照年龄从小到大对其所占用的大小进行累积，[当累积的某个年龄大小超过了survivor区的一半时，
    取这个年龄和MaxTenuringThreshold中更小的一个值，作为新的晋升年龄阈值]。
    在本案例中，调优前：Survivor区 = 64M，desired survivor = 32M，此时Survivor区中age<=2的对象累计大小为41M，
        41M大于32M，所以晋升年龄阈值被设置为2，下次Minor GC时将年龄超过2的对象被晋升到老年代。

JVM引入动态年龄计算，主要基于如下两点考虑：     下面的担保机制
    1.如果固定按照MaxTenuringThreshold设定的阈值作为晋升条件： 
        a）MaxTenuringThreshold设置的过大，原本应该晋升的对象一直停留在Survivor区，直到Survivor区溢出
            ，一旦溢出发生，Eden+Svuvivor中对象将不再依据年龄全部提升到老年代，这样对象老化的机制就失效了。 
        b）MaxTenuringThreshold设置的过小，“过早晋升”即对象不能在新生代充分被回收，大量短期对象被晋升到老年代，
            老年代空间迅速增长，引起频繁的Major GC。分代回收失去了意义，严重影响GC性能。
            相同应用在不同时间的表现不同：特殊任务的执行或者流量成分的变化，都会导致对象的生命周期分布发生波动，
            那么固定的阈值设定，因为无法动态适应变化，会造成和上面相同的问题。	
	
要讲这个Desired Survivor size就要知道一个参数：-XX:TargetSurvivorRatio
    这个参数的含义是：设定survivor区的目标使用率。默认50，即survivor区对象目标使用率为50%

这里就可以看到这个Desired survivor size的计算公式了：
    desired survivor size = (survivor区容量 * TargetSurvivorRatio)/100
        （其实就是survivor容量乘以这个targetSurvivorRatio的比值）
    这个TargetSurvivorRatio就是上面介绍的那个参数设置的值，默认是50，一般很少会去改。	


晋升老年代的总结
    1.担保机制
    新生代中垃圾收集采用的是复制算法，当Survivor区的内存大小不足以装下一次Minor Gc中所有的存活对象的时候，就启动担保机制，
        将Survivor不够放的活对象，直接进入到老年代。
    2.大对象直接进入老年代
        虚拟机提供了个-XX:pretenureSizeThreshold参数，令内存大于这个设置值的对象直接在老年代分配。
        这个参数只对Serial和ParNew收集器有效，Parallel Scavenge收集器不认识这个参数，一般它也不需要设置，
        如果遇到必须要设置这个参数的场合，可以考虑ParNew+CMS的收集器组合。
    3.长期存活的对象进入老年代
        就是上文说的，在Minor gc中，把age大于设置的-XX:MaxTenuringThresholed值的对象晋升到老年代。
        这个age是这样计算的，jvm为每个对象定义了一个对象年龄（Age）计数器，如果对象在Eden出生并经过第一次Minor GC后仍然存活，
        并能够被Survivor区容纳的话，将被移到Survivor区中，并且对象年龄设为1。
        对象在Survivor区中每“熬过”一次Minor GC，年龄就加一岁。
    4.动态对象年龄判断
        这里要说明下一个误区。
        书上是这样讲的：如果在Survivor空间中相同年龄所有对象大小的总和大于Survivor空间的一半，
        年龄大于或等于该年龄的对象就可以直接进入老年代，无须等到MaxTenuringThreshold中要求的年龄。
    然而我们上面分析过了：
        1. 不是某个年龄的对象总和，而是<=某个年龄的对象总和。
        2. 也不一定是大于SurVivor空间的一半，只是默认TargetSurvivorRatio设为50才是一半，应该是根据这个参数才对。


G1中几个重要概念
在G1的实现过程中，引入了一些新的概念，对于实现高吞吐、没有内存碎片、收集时间可控等功能起到了关键作用。下面我们就一起看一下G1中的这几个重要概念。

Region
传统的GC收集器将连续的内存空间划分为新生代、老年代和永久代（JDK 8去除了永久代，引入了元空间Metaspace），这种划分的特点是各代的存储地址
（逻辑地址，下同）是连续的。
---------------------------------------------------------------------------------------------------------
https://tech.meituan.com/2016/09/23/g1.html
G1 GC       与CMS相比：
    * G1是一个有整理内存过程的垃圾收集器，不会产生很多内存碎片。 
    * G1的Stop The World(STW)更可控，G1在停顿时间上添加了预测机制，用户可以指定期望停顿时间。
    传统的GC收集器将连续的内存空间划分为新生代、老年代和永久代（JDK 8去除了永久代，引入了元空间Metaspace），
        这种划分的特点是各代的存储地址（逻辑地址，下同）是连续的。如下图所示：
     Eden S0 S1         Tenured         Permanent   
        新生代             老年代         永久代
      
   而G1的各代存储地址是不连续的，每一代都使用了n个不连续的大小相同的Region，每个Region占有一块连续的虚拟内存地址  
      一些Region标明了H，它代表Humongous，这表示这些Region存储的是巨大对象（humongous object，H-obj），
      即大小大于等于region一半的对象。H-obj有如下几个特征：
       * H-obj直接分配到了old gen，防止了反复拷贝移动。 
       * H-obj在global concurrent marking阶段的cleanup 和 full GC阶段回收。 
       * 在分配H-obj之前先检查是否超过 initiating heap occupancy percent和the marking threshold, 
            如果超过的话，就启动global concurrent marking，为的是提早回收，防止 evacuation failures 和 full GC。
     为了减少连续H-objs分配对GC的影响，需要把大对象变为普通的对象，建议增大Region size。
     
# !一个Region的大小可以通过参数-XX:G1HeapRegionSize设定，取值范围从1M到32M，且是2的指数。如果不设定，那么G1会根据Heap大小自动决定 
 SATB
    全称是Snapshot-At-The-Beginning ==> GC开始时活着的对象的一个快照。它是通过Root Tracing得到的，作用是维持并发GC的正确性
 RSet
    全称是Remembered Set，是辅助GC过程的一种结构，典型的空间换时间工具，和Card Table有些类似。     
    还有一种数据结构也是辅助GC的：Collection Set（CSet），它记录了GC要收集的Region集合，集合里的Region可以是任意年代的。
    对于old->young和old->old的跨代对象引用，只要扫描对应的CSet中的RSet即可。
    
[逻辑上说每个Region都有一个RSet，RSet记录了其他Region中的对象引用本Region中对象的关系，
    属于points-into结构（谁引用了我的对象）。
 而Card Table则是一种points-out（我引用了谁的对象）的结构，每个Card 覆盖一定范围的Heap（一般为512Bytes）。
    G1的RSet是在Card Table的基础上实现的：每个Region会记录下别的Region有指向自己的指针，并标记这些指针分别在哪些Card的范围内。 
    这个RSet其实是一个Hash Table，Key是别的Region的起始地址，Value是一个集合，里面的元素是Card Table的Index。]
    
Pause Prediction Model 即停顿预测模型。它在G1中的作用是  
    G1 GC是一个响应时间优先的GC算法，它与CMS最大的不同是，用户可以设定整个GC过程的期望停顿时间，
        参数-XX:MaxGCPauseMillis指定一个G1收集过程目标停顿时间，默认值200ms，不过它不是硬性条件，只是期望值。
    那么G1怎么满足用户的期望呢？就需要这个停顿预测模型了。G1根据这个模型统计计算出来的历史数据来预测本次收集需要选择的Region数量，
        从而尽量满足用户设定的目标停顿时间。 停顿预测模型是以衰减标准偏差为理论基础实现的：  

G1提供了两种GC模式，Young GC和Mixed GC，两种都是完全Stop The World的。 
     * Young GC：选定所有年轻代里的Region。通过控制年轻代的region个数，即年轻代内存大小，来控制young GC的时间开销。 
     * Mixed GC：选定所有年轻代里的Region，外加根据global concurrent marking统计得出收集收益高的若干老年代Region。
        在用户指定的开销目标范围内尽可能选择收益高的老年代Region。
    
   Mixed GC不是full GC，它只能回收部分老年代的Region，如果mixed GC实在无法跟上程序分配内存的速度，
    导致老年代填满无法继续进行Mixed GC，就会使用serial old GC（full GC）来收集整个GC heap。
    所以我们可以知道，G1是不提供full GC的!!!
    
   Pause Prediction Model 是为 Mixed GC提供标记服务的 

global concurrent marking
    * 初始标记（initial mark，STW）。它标记了从GC Root开始直接可达的对象。 
    * 并发标记（Concurrent Marking）。这个阶段从GC Root开始对heap中的对象标记，标记线程与应用程序线程并行执行，
        并且收集各个Region的存活对象信息。 
    * 最终标记（Remark，STW）。标记那些在并发标记阶段发生变化的对象，将被回收。 
    * 清除垃圾（Cleanup）。清除空Region（没有存活对象的），加入到free list。   
        
--------------------------------------------------------------------------------------------------------------------        
        
当我们使用Server模式下的ParallelGC收集器组合（Parallel Scavenge+Serial Old的组合）下，
    担保机制的实现和之前的Client模式下（SerialGC收集器组合）有所变化。
在GC前还会进行一次判断，如果要分配的内存>=Eden区大小的一半，那么会直接把要分配的内存放入老年代中。否则才会进入担保机制。


#https://tech.meituan.com/2017/12/29/jvm-optimize.html 	
JVM是如何避免Minor GC时扫描全堆的？ 经过统计信息显示，老年代持有新生代对象引用的情况不足1%，
    根据这一特性JVM引入了卡表（card table）来实现这一目的。如下图所示：	
	卡表的具体策略是将老年代的空间分成大小为512B的若干张卡（card）。卡表本身是单字节数组，数组中的每个元素对应着一张卡，
	    当发生[老年代引用新生代时]，虚拟机将该卡对应的卡表元素设置为适当的值。
    如上图所示，卡表3被标记为脏（卡表还有另外的作用，标识并发标记阶段哪些块被修改过），
    之后Minor GC时通过扫描卡表就可以很快的识别哪些卡中存在老年代指向新生代的引用。这样虚拟机通过空间换时间的方式，避免了全堆扫描。
	
	
[CMS默认的回收线程数是(CPU个数+3)/4]。
    这个公式的意思是当CPU大于4个时,保证回收线程占用至少25%的CPU资源，这样用户线程占用75%的CPU，这是可以接受的。
    
CMS 4个阶段
    1. Init-mark初始标记(STW) ，该阶段进行可达性分析，标记GC ROOT能直接关联到的对象，所以很快。 
    2. Concurrent-mark并发标记，由前阶段标记过的绿色对象出发，所有可到达的对象都在本阶段中标记。 
    3. Remark重标记(STW) ，暂停所有用户线程，重新扫描堆中的对象，进行可达性分析，标记活着的对象。
        因为并发标记阶段是和用户线程并发执行的过程，所以该过程中可能有用户线程修改某些活跃对象的字段，指向了一个未标记过的对象，
        如下图中红色对象在并发标记开始时不可达，但是并行期间引用发生变化，变为对象可达，这个阶段需要重新标记出此类对象，
        防止在下一阶段被清理掉，这个过程也是需要STW的。特别需要注意一点，这个阶段是以新生代中对象为根来判断对象是否存活的。 
    4. 并发清理，进行并发的垃圾清理。	
    
   老年代的机制与一个叫CARD TABLE的东西（这个东西其实就是个数组,数组中每个位置存的是一个byte）密不可分。
    CMS将老年代的空间分成大小为512bytes的块，card table中的每个元素对应着一个块。
    并发标记时，如果某个对象的引用发生了变化，就标记该对象所在的块为  dirty card。
    并发预清理阶段就会重新扫描该块，将该对象引用的对象标识为可达。
    
   不过card table还有其他作用。
    还记得前面提到的那个问题么？进行Minor GC时,如果有老年代引用新生代，怎么识别？
    (有研究表明，在所有的引用中，老年代引用新生代这种场景不足1%.原因大家可以自己分析下)
    当有老年代引用新生代，对应的card table被标识为相应的值（card table中是一个byte，有八位，约定好每一位的含义就可区分哪个是引用新生代，哪个是并发标记阶段修改过的）。
    所以，Minor GC通过扫描card table就可以很快的识别老年代引用新生代。


生代GC和老年代的GC是各自分开独立进行的，只有Minor GC时才会使用根搜索算法，标记新生代对象是否可达，
    也就是说虽然一些对象已经不可达，但在Minor GC发生前不会被标记为不可达，CMS也无法辨认哪些对象存活，
    只能全堆扫描（新生代+老年代）。由此可见堆中对象的数目影响了Remark阶段耗时。	


什么时候可能会触发STW的Full GC呢？ 
    1. Perm空间不足； 
    2. CMS GC时出现promotion failed和concurrent mode failure（concurrent mode failure发生的原因一般是CMS正在进行，
        但是由于老年代空间不足，需要尽快回收老年代里面的不再被使用的对象，这时停止所有的线程，同时终止CMS，
        直接进行Serial Old GC）； 
    3. 统计得到的Young GC晋升到老年代的平均大小大于老年代的剩余空间； 
    4. 主动触发Full GC（执行jmap -histo:live [pid]）来避免碎片问题。
    

GC问题处理1：
    -XX:PermSize参数和-XX:MaxPermSize设置成一样，强制虚拟机在启动的时候就把永久代的容量固定下来，避免运行时自动扩容
    调整参数后，服务不再有Perm区扩容导致的STW GC发生。


https://tech.meituan.com/2019/01/03/spring-boot-native-memory-leak.html  
	Spring Boot引起的“堆外内存泄漏”排查及经验总结

https://www.toutiao.com/a6683710658250277380/  
	JVM性能调优篇










