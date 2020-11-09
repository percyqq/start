https://www.jianshu.com/p/e62fa839aa41


变量可见性 其一是保证该变量对所有线程可见，这里的可见性指的是当一个线程修改了变量的值，那么新的值对于其他线程是可以立即获取的。
禁止重排序 volatile 禁止了指令重排。



相比synchronized，ReentrantLock增加了一些高级功能。主要来说主要有三点:1等待可中断;2可
实现公平锁;3可实现选择性通知(锁可以绑定多个条件) 
1.ReentrantLock提供了一种能够中断等待锁的线程的机制，通过lock.lockInterruptibly()来实现这个机制。
    也就是说正在等待的线程可以选择放弃等待，改为处理其他事情。
2.ReentrantLock可以指定是公平锁还是非公平锁。而synchronized只能是非公平锁。所谓的公平 锁就是先等待的线程先获得锁。 
    ReentrantLock默认情况是非公平的，可以通过 ReentrantLock 类的ReentrantLock(boolean fair)构造方法来制定是否是公平的。 
3.synchronized关键字与wait()和notify()/notifyAll()方法相结合可以实现等待/通知机制， 
    ReentrantLock类当然也可以实现，但是需要借助于Condition接口与newCondition() 方法。 
    Condition是JDK1.5之后才有的，它具有很好的灵活性，比如可以实现多路通知功能也就是在一 个Lock对象中可以创建多个Condition实例(即对象监视器)，
    [线程对象可以注册在指定的 Condition中，从而可以有选择性的进行线程通知，在调度线程上更加灵活。 
    在使用 notify()/notifyAll()方法进行通知时，被通知的线程是由 JVM 选择的，用ReentrantLock类结 合Condition实例可以实现“选择性通知” ，]
    这个功能非常重要，而且是Condition接口默认提供 的。
    而synchronized关键字就相当于整个Lock对象中只有一个Condition实例，所有的线程都注 册在它一个身上。
    如果执行notifyAll()方法的话就会通知所有处于等待状态的线程这样会造成 很大的效率问题，
    而Condition实例的signalAll()方法 只会唤醒注册在该Condition实例中的所 有等待线程。
    如果你想使用上述功能，那么选择ReentrantLock是一个不错的选择。 
4 性能已不是选择标准


Synchronized是非公平锁。
JDK6对synchronized的实现机制进行了较大调整，包括使用JDK5引进的CAS自旋之外，还增加了自适应的CAS自旋、锁消除、锁粗化、偏向锁、轻量级锁这些优化策略。

1. JVM每次从队列的尾部取出一个数据用于锁竞争候选者（OnDeck），但是并发情况下，ContentionList会被大量的并发线程进行CAS访问，
  为了降低对尾部元素的竞争，JVM会将一部分线程移动到EntryList中作为候选竞争线程。 
2. Owner线程会在unlock时，将ContentionList中的部分线程迁移到EntryList中，并指定EntryList中的某个线程为OnDeck线程（一般是最先进去的那个线程）。
3. Owner线程并不直接把锁传递给OnDeck线程，而是把锁竞争的权利交给OnDeck，OnDeck需要重新竞争锁。这样虽然牺牲了一些公平性，
  但是能极大的提升系统的吞吐量，在JVM中，也把这种选择行为称之为“竞争切换”。 
4. OnDeck线程获取到锁资源后会变为Owner线程，而没有得到锁资源的仍然停留在EntryList中。如果Owner线程被wait方法阻塞，则转移到WaitSet队列中，
  直到某个时刻通过notify或者notifyAll唤醒，会重新进去EntryList中。 
5. 处于ContentionList、EntryList、WaitSet中的线程都处于阻塞状态，该阻塞是由操作系统来完成的（Linux内核下采用pthread_mutex_lock内核函数实现的）。
6. Synchronized是非公平锁。 Synchronized在线程进入ContentionList时，等待的线程会先尝试自旋获取锁，如果获取不到就进入ContentionList，
  这明显对于已经进入队列的线程是不公平的，还有一个不公平的事情就是自旋获取锁的线程还可能直接抢占OnDeck线程的锁资源。 
    参考：https://blog.csdn.net/zqz_zqz/article/details/70233767 
7. 每个对象都有个monitor对象，加锁就是在竞争monitor对象，代码块加锁是在前后分别加上monitorenter和monitorexit指令来实现的，
  方法加锁是通过一个标记位来判断的 
8. synchronized是一个重量级操作，需要调用操作系统相关接口，性能是低效的，有可能给线程加锁消耗的时间比有用操作消耗的时间更多。 
9. Java1.6，synchronized进行了很多的优化，有适应自旋、锁消除、锁粗化、轻量级锁及偏向锁等，效率有了本质上的提高。
  在之后推出的Java1.7与1.8中，均对该关键字的实现机理做了优化。引入了偏向锁和轻量级锁。都是在对象头中有标记位，不需要经过操作系统加锁。 
10. 锁可以从偏向锁升级到轻量级锁，再升级到重量级锁。这种升级过程叫做锁膨胀；
    锁是不能降级的~！
11. JDK 1.6中默认是开启偏向锁和轻量级锁，可以通过-XX:-UseBiasedLocking来禁用偏向锁。


引入偏向锁主要目的是：为了在没有多线程竞争的情况下尽量减少不必要的轻量级锁执行路径。因为轻量级锁的加锁解锁操作是需要依赖多次CAS原子指令的，
  而偏向锁只需要在置换ThreadID的时候依赖一次CAS原子指令（由于一旦出现多线程竞争的情况就必须撤销偏向锁，
  所以偏向锁的撤销操作的性能损耗也必须小于节省下来的CAS原子指令的性能消耗）。



Synchronized的作用主要有三个：
  1.原子性：确保线程互斥的访问同步代码；
  2.可见性：保证共享变量的修改能够及时可见，其实是通过Java内存模型中的 “对一个变量unlock操作之前，必须要同步到主内存中；如果对一个变量进行lock操作，
    则将会清空工作内存中此变量的值，在执行引擎使用此变量前，需要重新从主内存中load操作或assign操作初始化变量值” 来保证的；
  3.有序性：有效解决重排序问题，即 “一个unlock操作先行发生(happen-before)于后面对同一个锁的lock操作”；
  

Synchronized可以把任何一个非null对象作为"锁"，在HotSpot JVM实现中，锁有个专门的名字：对象监视器（Object Monitor）。

Synchronized总共有三种用法：
  当synchronized作用在实例方法时，监视器锁（monitor）便是对象实例（this）；
  当synchronized作用在静态方法时，监视器锁（monitor）便是对象的Class实例，因为Class数据存在于永久代，因此静态方法锁相当于该类的一个全局锁；
  当synchronized作用在某一个对象实例时，监视器锁（monitor）便是括号括起来的对象实例；
注意，synchronized 内置锁 是一种 对象锁（锁的是对象而非引用变量），作用粒度是对象 ，可以用来实现对 临界资源的同步互斥访问 ，是 可重入 的。
其可重入最大的作用是避免死锁，如：
  ====>  子类同步方法调用了父类同步方法，如没有可重入的特性，则会发生死锁；<=====



A.当一个线程访问同步代码块时，首先是需要得到锁才能执行同步代码，当退出或者抛出异常时必须要释放锁
monitorenter：每个对象都是一个监视器锁（monitor）。当monitor被占用时就会处于锁定状态，线程执行monitorenter指令时尝试获取monitor的所有权，过程如下：
  1.如果monitor的进入数为0，则该线程进入monitor，然后将进入数设置为1，该线程即为monitor的所有者；
  2.如果线程已经占有该monitor，只是重新进入，则进入monitor的进入数加1；
  3.如果其他线程已经占用了monitor，则该线程进入阻塞状态，直到monitor的进入数为0，再重新尝试获取monitor的所有权；

monitorexit：执行monitorexit的线程必须是objectref所对应的monitor的所有者。指令执行时，monitor的进入数减1，如果减1后进入数为0，那线程退出monitor，
  不再是这个monitor的所有者。其他被这个monitor阻塞的线程可以尝试去获取这个 monitor 的所有权。
  1.monitorexit指令出现了两次，第1次为同步正常退出释放锁；第2次为发生异步退出释放锁；

Synchronized的语义底层是通过一个monitor的对象来完成，其实wait/notify等方法也依赖于monitor对象，这就是为什么只有在同步的块或者方法中才能调用
  wait/notify等方法，否则会抛出java.lang.IllegalMonitorStateException的异常的原因。


B.方法的同步并没有通过指令 monitorenter 和 monitorexit 来完成（理论上其实也可以通过这两条指令来实现），不过相对于普通方法，
  其常量池中多了 ACC_SYNCHRONIZED 标示符。JVM就是根据该标示符来实现方法的同步的：

当方法调用时，调用指令将会检查方法的 ACC_SYNCHRONIZED 访问标志是否被设置，如果设置了，执行线程将先获取monitor，获取成功之后才能执行方法体，
  方法执行完后再释放monitor。在方法执行期间，其他任何线程都无法再获得同一个monitor对象。

两种同步方式本质上没有区别，只是方法的同步是一种隐式的方式来实现，无需通过字节码来完成。两个指令的执行是JVM通过调用操作系统的互斥原语mutex来实现，
被阻塞的线程会被挂起、等待重新调度，会导致“用户态和内核态”两个态之间来回切换，对性能有较大影响。



======================================================================================================================================
3.1 Java对象头
  在JVM中，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充。

  实例数据：存放类的属性数据信息，包括父类的属性信息；
  对齐填充：由于虚拟机要求 对象起始地址必须是8字节的整数倍。填充数据不是必须存在的，仅仅是为了字节对齐；
  对象头：Java对象头一般占有2个机器码（在32位虚拟机中，1个机器码等于4字节，也就是32bit，在64位虚拟机中，1个机器码是8个字节，也就是64bit），
    但是 如果对象是数组类型，则需要3个机器码，因为JVM虚拟机可以通过Java对象的元数据信息确定Java对象的大小，但是无法从数组的元数据来确认数组的大小，
    所以用一块来记录数组长度。
  
  Synchronized用的锁就是存在Java对象头里的，那么什么是Java对象头呢？Hotspot虚拟机的对象头主要包括两部分数据：Mark Word（标记字段）、
  Class Pointer（类型指针）。其中 Class Pointer是对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例，
  Mark Word用于存储对象自身的运行时数据，它是实现轻量级锁和偏向锁的关键。 
  
  对象头的最后两位存储了锁的标志位，01是初始状态，未加锁，其对象头里存储的是对象本身的哈希码，随着锁级别的不同，对象头里会存储不同的内容。
  偏向锁存储的是当前占用此对象的线程ID；而轻量级则存储指向线程栈中锁记录的指针。从这里我们可以看到，“锁”这个东西，
  可能是个锁记录+对象头里的引用指针（判断线程是否拥有锁时将线程的锁记录地址和对象头里的指针地址比较)，
  也可能是对象头里的线程ID（判断线程是否拥有锁时将线程的ID和对象头里存储的线程ID比较）。
  
  
  
  
3.2对象头中Mark Word与线程中Lock Record
  在线程进入同步代码块的时候，如果此同步对象没有被锁定，即它的锁标志位是01，则虚拟机首先在当前线程的栈中创建我们称之为“锁记录（Lock Record）”的空间，
  用于存储锁对象的Mark Word的拷贝，官方把这个拷贝称为Displaced Mark Word。整个Mark Word及其拷贝至关重要。

Lock Record是线程私有的数据结构，每一个线程都有一个可用Lock Record列表，同时还有一个全局的可用列表。每一个被锁住的对象Mark Word都会和
  一个Lock Record关联（对象头的MarkWord中的Lock Word指向Lock Record的起始地址），
  同时Lock Record中有一个Owner字段存放拥有该锁的线程的唯一标识（或者object mark word），表示该锁被这个线程占用。
  
  
  
  
  
  
