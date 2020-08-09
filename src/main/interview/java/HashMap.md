
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
    主要是从速度、功效、质量来考虑的，这么做可以在数组table的length比较小的时候，也能保证考虑到高低Bit都参与到Hash的计算中，
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
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    