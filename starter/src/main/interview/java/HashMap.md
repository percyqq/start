ConcurrentHashMap

JDK 1.7 中使用分段锁（ReentrantLock + Segment + HashEntry），相当于把一个 HashMap 分成多个段，每段分配一把锁，这样支持多线程访问。
    锁粒度：基于 Segment，包含多个 HashEntry。
JDK 1.8 中使用 CAS + synchronized + Node + 红黑树。锁粒度：Node（首结点）（实现 Map.Entry）。锁粒度降低了。

18.针对 ConcurrentHashMap 锁机制具体分析（JDK 1.7 VS JDK 1.8）
JDK 1.7 中，采用分段锁的机制，实现并发的更新操作，底层采用数组+链表的存储结构，包括两个核心静态内部类 Segment 和 HashEntry。
    ①、Segment 继承 ReentrantLock（重入锁） 用来充当锁的角色，每个 Segment 对象守护每个散列映射表的若干个桶；
    ②、HashEntry 用来封装映射表的键-值对；
    ③、每个桶是由若干个 HashEntry 对象链接起来的链表
JDK 1.8 中，采用Node + CAS + Synchronized来保证并发安全。取消类 Segment，直接用 table 数组存储键值对；
当 HashEntry 对象组成的链表长度超过 TREEIFY_THRESHOLD 时，链表转换为红黑树，提升性能。底层变更为数组 + 链表 + 红黑树。

19.ConcurrentHashMap 在 JDK 1.8 中，为什么要使用内置锁 synchronized 来代替重入锁 ReentrantLock？
    ①、粒度降低了；
    ②、JVM 开发团队没有放弃 synchronized，而且基于 JVM 的 synchronized 优化空间更大，更加自然。
    ③、在大量的数据操作下，对于 JVM 的内存压力，基于 API 的 ReentrantLock 会开销更多的内存。


@see  https://mp.weixin.qq.com/s/Nu3gewxwtCBvnAWii-S67g
ConcurrentHashMap 使用了分段计数： CounterCell 来计数。
    作者定义了一个数组来计数，而且这个用来计数的数组也能扩容，每次线程需要计数的时候，都通过随机的方式获取一个数组下标的位置进行操作，这样就可以尽可能的降低了锁的粒度，
    最后获取 size 时，则通过遍历数组来实现计数

ConcurrentHashMap 的并发度是什么？
    程序运行时能够同时更新 ConccurentHashMap 且不产生锁竞争的最大线程数。默认为 16，且可以在构造函数中设置。
    当用户设置并发度时，ConcurrentHashMap 会使用大于等于该值的最小2幂指数作为实际并发度（假如用户设置并发度为17，实际并发度则为32）

// 关于这个方法： Rando 在线程并发的时候会有性能问题，以及可能会产生相同的随机数，性能也没有ThreadLocalRandom.getProbe() 方法好
ThreadLocalRandom.getProbe()



方法一：
static final int hash(Object key) {   //jdk1.8 & jdk1.7
     int h;
     // h = key.hashCode() 为第一步 取hashCode值
     // h ^ (h >>> 16)  为第二步 高位参与运算
     return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

方法二：
static int indexFor(int h, int length) {  //jdk1.7的源码，jdk1.8没有这个方法，但是实现原理一样的
     return h & (length-1);  //第三步 取模运算
}

===> 相同：  h & (length-1) ====  h ^ (h >>> 16)
   h & (table.length -1)来得到该对象的保存位，而HashMap底层数组的长度总是2的n次方，这是HashMap在速度上的优化。
    当length总是2的n次方时，h& (length-1)运算等价于对length取模，也就是h%length，但是&比%具有更高的效率。

   在JDK1.8的实现中，优化了高位运算的算法，通过hashCode()的高16位异或低16位实现的：(h = k.hashCode()) ^ (h >>> 16)，
    主要是从速度、功效、质量来考虑的，这么做可以在数组table的length比较小的时候，也能保证考虑到高低Bit都参与到Hash的计算中，
    同时不会有太大的开销


java中的ConcurrentHashMap在jdk1.8之前的版本，使用一个Segment 数组
Segment< K,V >[] segments
    Segment继承自ReenTrantLock，所以每个Segment就是个可重入锁，每个Segment 有一个HashEntry< K,V >数组用来存放数据，
        put操作时，先确定往哪个Segment放数据，只需要锁定这个Segment，执行put，其它的Segment不会被锁定；
        所以数组中有多少个Segment就允许同一时刻多少个线程存放数据，这样增加了并发能力。


[重点优化]
1.7  
单链表的头插入方式，同一位置上新元素总会被放在链表的头部位置；
    这样先放在一个索引上的元素终会被放到Entry链的尾部(如果发生了hash冲突的话），这一点和Jdk1.8有区别，下文详解    
 
!!我们使用的是2次幂的扩展(指长度扩为原来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置   
    比如一个元素是5 ，对应0101， 此时length = 16  ==> 10000
    5 & (16 -1) ==  0101  ==> 00101  --> 5              同理： 15 & 15 = 15
                   01111                                      01111 
   扩容后
    5 & (32 -1) ==  0101  ==> 10101  --> 16 + 5         同理： 15 & 31 = 15
                  011111                                     001111  
    可以不用像jdk7那些重新计算hash！
    只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”
  
  这个设计确实非常的巧妙，既省去了重新计算hash值的时间，而且同时，由于新增的1bit是0还是1可以认为是随机的，
    因此resize的过程，均匀的把之前的冲突的节点分散到新的bucket了。
    这一块就是JDK1.8新增的优化点。
    
有一点注意区别，JDK1.7中rehash的时候，旧链表迁移新链表的时候，如果在新表的数组索引位置相同，则链表元素会倒置，
但是从上图可以看出，JDK1.8不会倒置。  
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    