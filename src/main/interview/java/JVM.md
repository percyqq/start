从 Java 8 开始，JDK 使用 invokedynamic 及 VM Anonymous Class 结合来 实现 Java 语言层面上的 Lambda 表达式。

？？记得遇到过 生成的匿名类过多，服务器的内存泄露问题。

JVM 回收线程的个数


-Xmx3550m：设置JVM最大堆内存为3550M。
-Xms3550m：设置JVM初始堆内存为3550M。此值可以设置与-Xmx相同，以避免每次垃圾回收完成后JVM重新分配内存。

-Xmn2g：设置年轻代大小为2G。在整个堆内存大小确定的情况下，增大年轻代将会减小年老代，反之亦然。
    此值关系到JVM垃圾回收，对系统性能影响较大，官方推荐配置为整个堆大小的3/8。

-XX:NewSize=1024m：设置年轻代初始值为1024M。
-XX:MaxNewSize=1024m：设置年轻代最大值为1024M。
-XX:PermSize=256m：设置持久代初始值为256M。
-XX:MaxPermSize=256m：设置持久代最大值为256M。

-XX:NewRatio=4：设置年轻代（包括1个Eden和2个Survivor区）与年老代的比值。表示年轻代比年老代为1:4。
-XX:SurvivorRatio=4：设置年轻代中Eden区与Survivor区的比值。表示2个Survivor区（JVM堆内存年轻代中默认有2个大小相等的Survivor区）与1个Eden区的比值为2:4，即1个Survivor区占整个年轻代大小的1/6。
-XX:MaxTenuringThreshold=7：表示一个对象如果在Survivor区（救助空间）移动了7次还没有被垃圾回收就进入年老代。如果设置为0的话，则年轻代对象不经过Survivor区，直接进入年老代，对于需要大量常驻内存的应用，这样做可以提高效率。如果将此值设置为一个较大值，则年轻代对象会在Survivor区进行多次复制，这样可以增加对象在年轻代存活时间，增加对象在年轻代被垃圾回收的概率，减少Full GC的频率，这样做可以在某种程度上提高服务稳定性。


空间分配担保  
    在发生 Minor GC 之前，虚拟机会先检查老年代最大可用的连续空间是否大于新生代所有对象总空间，如果这个条件成立，那么 Minor GC 可以确保是安全的。
    如果不成立，则虚拟机会查看 HandlePromotionFailure 设置值是否允许担保失败。
    如果允许，那么会继续检查老年代最大可能连续空间是否大于历次晋升到老年代对象的平均大小，
    如果大于，将尝试着进行一次 Minor GC，尽管这个 Minor GC 是有风险的；
    如果小于，或 HandlePromotionFailure 设置不允许冒险，那这次也要改为进行一次 Full GC。



在内存担保机制下，无法安置的对象会直接进到老年代，以下几种情况也会进入老年代。

大对象
    大对象指需要大量连续内存空间的对象，这部分对象不管是不是“朝生夕死”，都会直接进到老年代。这样做主要是为了避免在 Eden 区及2个 Survivor 区之间发生大量的内存复制。当你的系统有非常多“朝生夕死”的大对象时，得注意了。

长期存活对象
    虚拟机给每个对象定义了一个对象年龄（Age）计数器。正常情况下对象会不断的在 Survivor 的 From 区与 To 区之间移动，对象在 Survivor 区中每经历一次 Minor GC，年龄就增加1岁。当年龄增加到15岁时，这时候就会被转移到老年代。当然，这里的15，JVM 也支持进行特殊设置。

动态对象年龄
    虚拟机并不重视要求对象年龄必须到15岁，才会放入老年区，如果 Survivor 空间中相同年龄所有对象大小的总合大于 Survivor 空间的一半，年龄大于等于该年龄的对象就可以直接进去老年区，无需等你“成年”。

这其实有点类似于负载均衡，轮询是负载均衡的一种，保证每台机器都分得同样的请求。看似很均衡，但每台机的硬件不通，健康状况不同，我们还可以基于每台机接受的请求数，或每台机的响应时间等，来调整我们的负载均衡算法。





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

https://tech.meituan.com/2019/01/03/spring-boot-native-memory-leak.html  
	Spring Boot引起的“堆外内存泄漏”排查及经验总结

https://www.toutiao.com/a6683710658250277380/  
	JVM性能调优篇

https://blog.csdn.net/zqz_zqz/article/details/70233767  
java 中的锁 -- 偏向锁、轻量级锁、自旋锁、重量级锁  

  
G1中几个重要概念
在G1的实现过程中，引入了一些新的概念，对于实现高吞吐、没有内存碎片、收集时间可控等功能起到了关键作用。下面我们就一起看一下G1中的这几个重要概念。

Region
传统的GC收集器将连续的内存空间划分为新生代、老年代和永久代（JDK 8去除了永久代，引入了元空间Metaspace），这种划分的特点是各代的存储地址
（逻辑地址，下同）是连续的。如下图所示：
  



  

  



但是这个CAS有没有问题呢？肯定是有的。比如说大量的线程同时并发修改一个AtomicInteger，可能有很多线程会不停的自旋，进入一个无限重复的循环中。
这些线程不停地获取值，然后发起CAS操作，但是发现这个值被别人改过了，于是再次进入下一个循环，获取值，发起CAS操作又失败了，再次进入下一个循环。
在大量线程高并发更新AtomicInteger的时候，这种问题可能会比较明显，导致大量线程空循环，自旋转，性能和效率都不是特别好。
于是，当当当当，Java 8推出了一个新的类，LongAdder，他就是尝试使用分段CAS以及自动分段迁移的方式来大幅度提升多线程高并发执行CAS操作的性能！

在LongAdder的底层实现中，首先有一个base值，刚开始多线程来不停的累加数值，都是对base进行累加的，比如刚开始累加成了base = 5。
接着如果发现并发更新的线程数量过多，就会开始施行分段CAS的机制，也就是内部会搞一个Cell数组，每个数组是一个数值分段。
这时，让大量的线程分别去对不同Cell内部的value值进行CAS累加操作，这样就把CAS计算压力分散到了不同的Cell分段数值中了！
这样就可以大幅度的降低多线程并发更新同一个数值时出现的无限循环的问题，大幅度提升了多线程并发更新数值的性能和效率！
而且他内部实现了自动分段迁移的机制，也就是如果某个Cell的value执行CAS失败了，那么就会自动去找另外一个Cell分段内的value值进行CAS操作。
这样也解决了线程空旋转、自旋不停等待执行CAS操作的问题，让一个线程过来执行CAS时可以尽快的完成这个操作。
最后，如果你要从LongAdder中获取当前累加的总值，就会把base值和所有Cell分段数值加起来返回给你。





CAS虽然很高效，但是它也存在三大问题，这里也简单说一下：

ABA问题。CAS需要在操作值的时候检查内存值是否发生变化，没有发生变化才会更新内存值。但是如果内存值原来是A，后来变成了B，然后又变成了A，
	那么CAS进行检查时会发现值没有发生变化，但是实际上是有变化的。ABA问题的解决思路就是在变量前面添加版本号，每次变量更新的时候都把版本号加一，
	这样变化过程就从“A－B－A”变成了“1A－2B－3A”。
JDK从1.5开始提供了AtomicStampedReference类来解决ABA问题，具体操作封装在compareAndSet()中。compareAndSet()首先检查当前引用和当前标志与预期引用和预期标志是否相等，
如果都相等，则以原子方式将引用值和标志的值设置为给定的更新值。

循环时间长开销大。CAS操作如果长时间不成功，会导致其一直自旋，给CPU带来非常大的开销。
只能保证一个共享变量的原子操作。对一个共享变量执行操作时，CAS能够保证原子操作，但是对多个共享变量操作时，CAS是无法保证操作的原子性的。
Java从1.5开始JDK提供了AtomicReference类来保证引用对象之间的原子性，可以把多个变量放在一个对象里来进行CAS操作。




举个例子，比如说ReentrantLock、ReentrantReadWriteLock底层都是基于AQS来实现的。
那么AQS的全称是什么呢？AbstractQueuedSynchronizer，抽象队列同步器我们来看上面的图。
说白了，ReentrantLock内部包含了一个AQS对象，也就是AbstractQueuedSynchronizer类型的对象。

这个AQS对象就是ReentrantLock可以实现加锁和释放锁的关键性的核心组件。AQS对象内部有一个核心的变量叫做state，是int类型的，代表了加锁的状态。初始状态下，这个state的值是0。
另外，这个AQS内部还有一个关键变量，用来记录当前加锁的是哪个线程，初始化状态下，这个变量是null。

就是并发包里的一个核心组件，里面有state变量、加锁线程变量等核心的东西，维护了加锁状态。

这个ReentrantLock之所以用Reentrant打头，意思就是他是一个可重入锁。
可重入锁的意思，就是你可以对一个ReentrantLock对象多次执行lock()加锁和unlock()释放锁，也就是可以对一个锁加多次，叫做可重入加锁。





Java对象头
synchronized是悲观锁，在操作同步资源之前需要给同步资源先加锁，这把锁就是存在Java对象头里的，而Java对象头又是什么呢？

	我们以Hotspot虚拟机为例，Hotspot的对象头主要包括两部分数据：Mark Word（标记字段）、Klass Pointer（类型指针）。

	Mark Word：默认存储对象的HashCode，分代年龄和锁标志位信息。这些信息都是与对象自身定义无关的数据，所以Mark Word被设计成一个非固定的数据结构以便在极小的空间内存存储尽量多的数据。
		它会根据对象的状态复用自己的存储空间，也就是说在运行期间Mark Word里存储的数据会随着锁标志位的变化而变化。

	Klass Point：对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例


Monitor可以理解为一个同步工具或一种同步机制，通常被描述为一个对象。每一个Java对象就有一把看不见的锁，称为内部锁或者Monitor锁。
	Monitor是线程私有的数据结构，每一个线程都有一个可用monitor record列表，同时还有一个全局的可用列表。每一个被锁住的对象都会和一个monitor关联，同时monitor中有一个Owner字段存放拥有该锁的线程的唯一标识，表示该锁被这个线程占用。

synchronized通过Monitor来实现线程同步，Monitor是依赖于底层的操作系统的Mutex Lock（互斥锁）来实现的线程同步。


Method area is created on virtual machine startup, shared among all Java virtual machine threads and it is logically part of heap area. It stores per-class structures such as the run-time constant pool, field and method data, and the code for methods and constructors.
方法区在虚拟机启动的时候创建，共享给了所有虚机中的线程。在逻辑上可以认为是 Heap 区的一部分。它存储了每个class 的结构。例如：
运行时常量池，字段和方法的数据，和 方法/构造器的代码。




从Java 8开始，JDK使用invokedynamic及VM Anonymous Class结合来实现Java语言层面上的Lambda表达式。

invokedynamic： invokedynamic是Java 7为了实现在JVM上运行动态语言而引入的一条新的虚拟机指令，它可以实现在运行期动态解析出调用点限定符所引用的方法，然后再执行该方法，invokedynamic指令的分派逻辑是由用户设定的引导方法决定。
VM Anonymous Class：可以看做是一种模板机制，针对于程序动态生成很多结构相同、仅若干常量不同的类时，可以先创建包含常量占位符的模板类，
而后通过Unsafe.defineAnonymousClass方法定义具体类时填充模板的占位符生成具体的匿名类。生成的匿名类不显式挂在任何ClassLoader下面，只要当该类没有存在的实例对象、
且没有强引用来引用该类的Class对象时，该类就会被GC回收。故而VM Anonymous Class相比于Java语言层面的匿名内部类无需通过ClassClassLoader进行类加载且更易回收。

在Lambda表达式实现中，通过invokedynamic指令调用引导方法生成调用点，在此过程中，会通过ASM动态生成字节码，而后利用Unsafe的defineAnonymousClass方法定义实现相应的函数式接口的匿名类，
然后再实例化此匿名类，并返回与此匿名类中函数式方法的方法句柄关联的调用点；而后可以通过此调用点实现调用相应Lambda表达式定义逻辑的功能。


典型应用
常规对象实例化方式：我们通常所用到的创建对象的方式，从本质上来讲，都是通过new机制来实现对象的创建。但是，new机制有个特点就是当类只提供有参的构造函数且无显示声明无参构造函数时，
则必须使用有参构造函数进行对象构造，而使用有参构造函数时，必须传递相应个数的参数才能完成对象实例化。
非常规的实例化方式：而Unsafe中提供allocateInstance方法，仅通过Class对象就可以创建此类的实例对象，而且不需要调用其构造函数、初始化代码、JVM安全检查等。
它抑制修饰符检测，也就是即使构造器是private修饰的也能通过此方法实例化，只需提类对象即可创建相应的对象。
由于这种特性，allocateInstance在java.lang.invoke、Objenesis（提供绕过类构造器的对象生成方式）、Gson（反序列化时用到）中都有相应的应用。


ASM	  访问者模式。

JVMTI & Agent & Attach API
上一小节中，我们给出了Agent类的代码，追根溯源需要先介绍JPDA（Java Platform Debugger Architecture）


https://tech.meituan.com/2019/02/28/java-dynamic-trace.html

java.lang.instrument.Instrumentation
这么两个接口	redefineClasses和retransformClasses。一个是重新定义class，一个是修改class。
BTrace基于ASM、Java Attach Api、Instruments开发，为用户提供了很多注解。依靠这些注解，我们可以编写BTrace脚本（简单的Java代码）达到我们想要的效果，而不必深陷于ASM对字节码的操作中不可自拔。

BTrace最终借Instruments实现class的替
基于Java的Attach Api，Agent可以动态附着到一个运行的JVM上，然后开启一个BTrace Server，接收client发过来的BTrace脚本；解析脚本，然后根据脚本中的规则找到要修改的类；
修改字节码后，调用Java Instrument的reTransform接口，完成对对象行为的修改并使之生效。

如上文所说，出于安全考虑，Instruments在使用上存在诸多的限制，BTrace也不例外。BTrace对JVM来说是“只读的”，因此BTrace脚本的限制如下：
不允许创建对象
不允许创建数组
不允许抛异常
不允许catch异常
不允许随意调用其他对象或者类的方法，只允许调用com.sun.btrace.BTraceUtils中提供的静态方法（一些数据处理和信息输出工具）
不允许改变类的属性
不允许有成员变量和方法，只允许存在static public void方法
不允许有内部类、嵌套类
不允许有同步方法和同步块
不允许有循环
不允许随意继承其他类（当然，java.lang.Object除外）
不允许实现接口
不允许使用assert
不允许使用Class对象






