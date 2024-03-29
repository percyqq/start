
https://blog.csdn.net/zqz_zqz/article/details/70233767  
java 中的锁 -- 偏向锁、轻量级锁、自旋锁、重量级锁  

   自旋等待的时间必须要有一定的限度，如果自旋超过了限定次数（默认是10次，可以使用-XX:PreBlockSpin来更改）没有成功获得锁，
    就应当挂起线程。
   自旋锁的实现原理同样也是CAS，AtomicInteger中调用unsafe进行自增操作的源码中的do-while循环就是一个自旋操作，
    如果修改数值失败则通过循环来执行自旋，直至修改成功。
   
   自旋锁在JDK1.4.2中引入，使用-XX:+UseSpinning来开启。JDK 6中变为默认开启，并且引入了自适应的自旋锁（适应性自旋锁）。
   自适应意味着自旋的时间（次数）不再固定，而是由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定。
    1.如果在同一个锁对象上，自旋等待刚刚成功获得过锁，并且持有锁的线程正在运行中，那么虚拟机就会认为这次自旋也是很有可能再次成功，
        进而它将允许自旋等待持续相对更长的时间。
    2.如果对于某个锁，自旋很少成功获得过，那在以后尝试获取这个锁时将可能省略掉自旋过程，直接阻塞线程，避免浪费处理器资源。
   
   在自旋锁中 另有三种常见的锁形式:TicketLock、CLHlock和MCSlock.


synchronized，synchronized通过Monitor来实现线程同步，Monitor是依赖于底层的操作系统的Mutex Lock（互斥锁）来实现的线程同步。
#锁状态	        存储内容	                                        存储内容
无锁	            对象的hashCode、对象分代年龄、是否是偏向锁（0）	    01
偏向锁	        偏向线程ID、偏向时间戳、对象分代年龄、是否是偏向锁（1）	01
轻量级锁	        指向栈中锁记录的指针	                            00
重量级锁	        指向互斥量（重量级锁）的指针	                        10

1.无锁的特点就是修改操作在循环内进行，线程会不断的尝试修改共享资源。如果没有冲突就修改成功并退出，否则就会继续循环尝试。
    如果有多个线程修改同一个值，必定会有一个线程能修改成功，而其他修改失败的线程会不断重试直到修改成功。
    上面我们介绍的CAS原理及应用即是无锁的实现。无锁无法全面代替有锁，但无锁在某些场合下的性能是非常高的。

2.偏向锁
    偏向锁是指一段同步代码一直被一个线程所访问，那么该线程会自动获取锁，降低获取锁的代价。
    在大多数情况下，锁总是由同一线程多次获得，不存在多线程竞争，所以出现了偏向锁。其目标就是在只有一个线程执行同步代码块时能够提高性能。
    当一个线程访问同步代码块并获取锁时，会在Mark Word里存储锁偏向的线程ID。
    在线程进入和退出同步块时不再通过CAS操作来加锁和解锁，而是检测Mark Word里是否存储着指向当前线程的偏向锁。
    引入偏向锁是为了在无多线程竞争的情况下尽量减少不必要的轻量级锁执行路径，因为轻量级锁的获取及释放依赖多次CAS原子指令，
    而偏向锁只需要在置换ThreadID的时候依赖一次CAS原子指令即可。

3.轻量级锁 是指当锁是偏向锁的时候，被另外的线程所访问，偏向锁就会升级为轻量级锁，其他线程会通过自旋的形式尝试获取锁，不会阻塞，从而提高性能。
    在代码进入同步块的时候，如果同步对象锁状态为无锁状态（锁标志位为“01”状态，是否为偏向锁为“0”），
    虚拟机首先将在当前线程的[栈帧]中建立一个名为[锁记录（Lock Record）]的空间，用于存储锁对象目前的Mark Word的拷贝，
    然后拷贝对象头中的Mark Word复制到锁记录中。
    拷贝成功后，虚拟机将使用CAS操作尝试将[对象的Mark Word]更新为指向Lock Record的指针，并将Lock Record里的owner指针指向对象的Mark Word。
    如果这个更新动作成功了，那么这个线程就拥有了该对象的锁，并且对象Mark Word的锁标志位设置为“00”，表示此对象处于轻量级锁定状态。
    如果轻量级锁的更新操作失败了，虚拟机首先会检查对象的Mark Word是否指向当前线程的栈帧，
        如果是就说明当前线程已经拥有了这个对象的锁，那就可以直接进入同步块继续执行，否则说明多个线程竞争锁。
    若当前只有一个等待线程，则该线程通过自旋进行等待。但是当自旋超过一定的次数，或者一个线程在持有锁，一个在自旋，又有第三个来访时，轻量级锁升级为重量级锁。
4.重量级锁
升级为重量级锁时，锁标志的状态值变为“10”，此时Mark Word中存储的是指向重量级锁的指针，此时等待锁的线程都会进入阻塞状态。


https://tech.meituan.com/2018/11/15/java-lock.html
无锁 VS 偏向锁 VS 轻量级锁 VS 重量级锁  这四种锁是指锁的状态，专门针对synchronized的。

Synchronized的实现
    Synchronized是非公平锁。 Synchronized在线程进入ContentionList时，等待的线程会先尝试自旋获取锁，
        如果获取不到就进入ContentionList，这明显对于已经进入队列的线程是不公平的，
        [还有一个不公平的事情就是自旋获取锁的线程还可能直接抢占OnDeck线程的锁资源。]


  它有多个队列，当多个线程一起访问某个对象监视器的时候，对象监视器会将这些线程存储在不同的容器中。
    1. Contention List：竞争队列，所有请求锁的线程首先被放在这个竞争队列中；
    2. Entry List：Contention List中那些有资格成为候选资源的线程被移动到Entry List中；
    3. Wait Set：哪些调用wait方法被阻塞的线程被放置在这里；
    4. OnDeck：任意时刻，最多只有一个线程正在竞争锁资源，该线程被成为OnDeck；
    5. Owner：当前已经获取到所资源的线程被称为Owner；
    6. !Owner：当前释放锁的线程。

偏向锁的适用场景
    始终只有一个线程在执行同步块，在它没有执行完释放锁之前，没有其它线程去执行同步块，在锁无竞争的情况下使用，
        一旦有了竞争就升级为轻量级锁，升级为轻量级锁的时候需要撤销偏向锁，撤销偏向锁的时候会导致stop the word操作；
    在有锁的竞争时，偏向锁会多做很多额外操作，尤其是撤销偏向所的时候会导致进入安全点，安全点会导致stw，导致性能下降，
        这种情况下应当禁用；

轻量级锁是由偏向所升级来的，偏向锁运行在一个线程进入同步块的情况下，当第二个线程加入锁争用的时候，偏向锁就会升级为轻量级锁；



但是这个CAS有没有问题呢？肯定是有的。
    比如说大量的线程同时并发修改一个AtomicInteger，可能有很多线程会不停的自旋，进入一个无限重复的循环中。
    这些线程不停地获取值，然后发起CAS操作，但是发现这个值被别人改过了，于是再次进入下一个循环，获取值，
        发起CAS操作又失败了，再次进入下一个循环。
    在大量线程高并发更新AtomicInteger的时候，这种问题可能会比较明显，导致大量线程空循环，自旋转，性能和效率都不是特别好。
    于是，当当当当，Java 8推出了一个新的类，LongAdder，
        他就是尝试使用分段CAS以及自动分段迁移的方式来大幅度提升多线程高并发执行CAS操作的性能！

在LongAdder的底层实现中，首先有一个base值，刚开始多线程来不停的累加数值，都是对base进行累加的，比如刚开始累加成了base = 5。
    接着如果发现并发更新的线程数量过多，就会开始施行分段CAS的机制，也就是内部会搞一个Cell数组，每个数组是一个数值分段。
    这时，让大量的线程分别去对不同Cell内部的value值进行CAS累加操作，这样就把CAS计算压力分散到了不同的Cell分段数值中了！
    这样就可以大幅度的降低多线程并发更新同一个数值时出现的无限循环的问题，大幅度提升了多线程并发更新数值的性能和效率！
    而且他内部实现了自动分段迁移的机制，也就是如果某个Cell的value执行CAS失败了，那么就会自动去找另外一个Cell分段内的value值进行CAS操作。
    这样也解决了线程空旋转、自旋不停等待执行CAS操作的问题，让一个线程过来执行CAS时可以尽快的完成这个操作。
    最后，如果你要从LongAdder中获取当前累加的总值，就会把base值和所有Cell分段数值加起来返回给你。


CAS虽然很高效，但是它也存在三大问题，这里也简单说一下：

ABA问题。CAS需要在操作值的时候检查内存值是否发生变化，没有发生变化才会更新内存值。
    但是如果内存值原来是A，后来变成了B，然后又变成了A，
	那么CAS进行检查时会发现值没有发生变化，但是实际上是有变化的。
	ABA问题的解决思路就是在变量前面添加版本号，每次变量更新的时候都把版本号加一，
	这样变化过程就从“A－B－A”变成了“1A－2B－3A”。
JDK从1.5开始提供了AtomicStampedReference类来解决ABA问题，具体操作封装在compareAndSet()中。
    compareAndSet()首先检查当前引用和当前标志与预期引用和预期标志是否相等，
    如果都相等，则以原子方式将引用值和标志的值设置为给定的更新值。

循环时间长开销大。CAS操作如果长时间不成功，会导致其一直自旋，给CPU带来非常大的开销。
    只能保证一个共享变量的原子操作。对一个共享变量执行操作时，CAS能够保证原子操作，
    但是对多个共享变量操作时，CAS是无法保证操作的原子性的。
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
