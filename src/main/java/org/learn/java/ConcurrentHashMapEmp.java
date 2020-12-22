package org.learn.java;

import java.util.ArrayList;

/**
 * @create: 2020-07-24 16:16
 */
public class ConcurrentHashMapEmp {

    ArrayList l;

    //https://mp.weixin.qq.com/s?__biz=MzAxNjk4ODE4OQ==&mid=2247490562&idx=3&sn=0f90a45efbff73dfbc311ec34fb2233a

    /**
     * 16.HashMap & ConcurrentHashMap 的区别？
     * 除了加锁，原理上无太大区别。另外，HashMap 的键值对允许有null，但是ConCurrentHashMap 都不允许。
     *
     *
     * 17.为什么 ConcurrentHashMap 比 HashTable 效率要高？
     * HashTable 使用一把锁（锁住整个链表结构）处理并发问题，多个线程竞争一把锁，容易阻塞；
     *
     * ConcurrentHashMap
     * JDK 1.7 中使用分段锁（ReentrantLock + Segment + HashEntry），相当于把一个 HashMap 分成多个段，每段分配一把锁，这样支持多线程访问。
     *  锁粒度：基于 Segment，包含多个 HashEntry。
     *
     * JDK 1.8 中使用 CAS + synchronized + Node + 红黑树。锁粒度：Node（首结点）（实现 Map.Entry）。锁粒度降低了。
     *
     *
     * 18.针对 ConcurrentHashMap 锁机制具体分析（JDK 1.7 VS JDK 1.8）
     * JDK 1.7 中，采用分段锁的机制，实现并发的更新操作，底层采用数组+链表的存储结构，包括两个核心静态内部类 Segment 和 HashEntry。
     *
     * ①、Segment 继承 ReentrantLock（重入锁） 用来充当锁的角色，每个 Segment 对象守护每个散列映射表的若干个桶；
     * ②、HashEntry 用来封装映射表的键-值对；
     * ③、每个桶是由若干个 HashEntry 对象链接起来的链表
     *
     * JDK 1.8 中，采用Node + CAS + Synchronized来保证并发安全。取消类 Segment，直接用 table 数组存储键值对；当 HashEntry 对象组成的链表长度超过 TREEIFY_THRESHOLD 时，链表转换为红黑树，提升性能。底层变更为数组 + 链表 + 红黑树。
     *
     *
     * 19.ConcurrentHashMap 在 JDK 1.8 中，为什么要使用内置锁 synchronized 来代替重入锁 ReentrantLock？
     * ①、粒度降低了；
     * ②、JVM 开发团队没有放弃 synchronized，而且基于 JVM 的 synchronized 优化空间更大，更加自然。
     * ③、在大量的数据操作下，对于 JVM 的内存压力，基于 API 的 ReentrantLock 会开销更多的内存。
     *
     *
     * 20.ConcurrentHashMap 简单介绍？
     * ①、重要的常量：
     * private transient volatile int sizeCtl;
     *
     * 当为负数时，-1 表示正在初始化，-N 表示 N - 1 个线程正在进行扩容；
     * 当为 0 时，表示 table 还没有初始化；
     * 当为其他正数时，表示初始化或者下一次进行扩容的大小。
     *
     * ②、数据结构：
     * Node 是存储结构的基本单元，继承 HashMap 中的 Entry，用于存储数据；
     * TreeNode 继承 Node，但是数据结构换成了二叉树结构，是红黑树的存储结构，用于红黑树中存储数据；
     * TreeBin 是封装 TreeNode 的容器，提供转换红黑树的一些条件和锁的控制。
     *
     * ③、存储对象时（put() 方法）：
     * 如果没有初始化，就调用 initTable() 方法来进行初始化；
     * 如果没有 hash 冲突就直接 CAS 无锁插入；
     * 如果需要扩容，就先进行扩容；
     * 如果存在 hash 冲突，就加锁来保证线程安全，两种情况：
     *      一种是链表形式就直接遍历 到尾端插入，一种是红黑树就按照红黑树结构插入；
     *
     * 如果该链表的数量大于阀值 8，就要先转换成红黑树的结构，break 再一次进入循环
     * 如果添加成功就调用 addCount() 方法统计 size，并且检查是否需要扩容。
     *
     * ④、扩容方法 transfer()：默认容量为 16，扩容时，容量变为原来的两倍。
     * helpTransfer()：调用多个工作线程一起帮助进行扩容，这样的效率就会更高。
     *
     * ⑤、获取对象时（get()方法）：
     * 计算 hash 值，定位到该 table 索引位置，如果是首结点符合就返回；
     * 如果遇到扩容时，会调用标记正在扩容结点 ForwardingNode.find()方法，查找该结点，匹配就返回；
     * 以上都不符合的话，就往下遍历结点，匹配就返回，否则最后就返回 null。
     *
     * 21.ConcurrentHashMap 的并发度是什么？
     * 程序运行时能够同时更新 ConccurentHashMap 且不产生锁竞争的最大线程数。默认为 16，且可以在构造函数中设置。
     * 当用户设置并发度时，ConcurrentHashMap 会使用大于等于该值的最小2幂指数作为实际并发度（假如用户设置并发度为17，实际并发度则为32）
     *
     * */
}
