## https://mp.weixin.qq.com/s?__biz=Mzg3NjU3NTkwMQ==&mid=2247505103&idx=1&sn=a041dbec689cec4f1bbc99220baa7219&scene=21
核心参数： 
corePoolSize：（核心线程数大小：不管它们创建以后是不是空闲的。线程池需要保持 corePoolSize 数量的线程，除非设置了 allowCoreThreadTimeOut。）
maximumPoolSize：（最大线程数：线程池中最多允许创建 maximumPoolSize 个线程。）
keepAliveTime：（存活时间：如果经过 keepAliveTime 时间后，超过核心线程数的线程还没有接受到新的任务，那就回收。）
workQueue：（存放待执行任务的队列：当提交的任务数超过核心线程数大小后，再提交的任务就存放在这里。它仅仅用来存放被 execute 方法提交的 Runnable 任务。
        所以这里就不要翻译为工作队列了，好吗？不要自己给自己挖坑。）


# https://mp.weixin.qq.com/s/FJQ5MhB1kMp8lP1NA6q4Vg  线程池动态调整。
线程池被创建后里面有线程吗？如果没有的话，你知道有什么方法对线程池进行预热吗？
    [线程池被创建后如果没有任务过来，里面是不会有线程的。如果需要预热的话可以调用下面的两个方法：]  
        仅启动一个：prestartCoreThread(),     启动全部： prestartAllCoreThreads()

核心线程数会被回收吗？需要什么设置？
    [核心线程数默认是不会被回收的，如果需要回收核心线程数，需要调用下面的方法：]
    allowCoreThreadTimeOut()


让你设计一个连接池你怎么设计？
    你答：我之前看过 HikariCP 的源码。
重点就是无锁设计，因为连接池是读多写少的场景，所以可以利用 CopyOnWriteArrayList 来存储连接，然后再利用本地化存储的思想来减少竞争。
获取连接的流程：
    1. 先去 ThreadLocal 找之前用过的连接，找到则直接返回。
    2. 如果找不到就去 CopyOnWriteArrayList 实现的  sharedList 里面找连接(这里还有个窃取的概念)，如果找到则返回。
    3. 如果找不到则用 SynchronousQueue 等待连接，超时则返回 null。

线程池的方法  ThreadPoolExecutor.interruptIdleWorkers
在这个方法里面进来第一件事就是拿 mainLock 锁，然后尝试去做中断线程的操作。
由于有 mainLock.lock 的存在，所以多个线程调用这个方法，就被 serializes 串行化了起来。
    串行化起来的好处是什么呢？
    就是后面接着说的：[避免了不必要的中断风暴（interrupt storms），尤其是调用 shutdown 方法的时候，避免退出的线程再次中断那些尚未中断的线程。]

# https://mp.weixin.qq.com/s/hduWrrK4B8x8Z3C7RnIhjw
Worker 是继承自 AQS 对象   ==>  不用 ReentrantLock 而是选择了自己搞一个 worker 类。
        [因为他想要的是一个不能重入的互斥锁，而 ReentrantLock 是可以重入的。
        自定义 worker 类的大前提是为了维护中断状态，因为正在执行任务的线程是不应该被中断的。]
正在执行任务的线程是不应该被中断的。tryLock 中的核心逻辑compareAndSetState(0, 1)，就是一个上锁的操作
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    w.unlock(); // allow interrupts

}

https://mp.weixin.qq.com/s?__biz=MzIxNTQ4MzE1NA==&mid=2247483741&idx=1&sn=238fc933c3b9b19ab1754b23283ac6fd&scene=21#wechat_redirect
当一个线程池里面的线程异常后:
    1.当执行方式是execute时,可以看到堆栈异常的输出。
    2.当执行方式是submit时,堆栈异常没有输出。但是调用Future.get()方法时，可以捕获到异常。
    3.不会影响线程池里面其他线程的正常执行。
    4.线程池会把这个线程移除掉，并创建一个新的线程放到线程池中。

private final class Worker extends AbstractQueuedSynchronizer implements Runnable{
    public void run() {
        runWorker(this);
        // ==>  
        final void runWorker(Worker w) {
            //
            //运行完会删掉worker
            workers.remove(w);    
        }
    }
}
    


[newCachedThreadPool 使用的是 SynchronousQueue，其他的是LinkedBlockingQueue]

SynchronousQueue没有容量，是无缓冲等待队列，是一个不存储元素的阻塞队列，会直接将任务交给消费者，
    必须等队列中的添加元素被消费后才能继续添加新的元素。
拥有公平（FIFO）和非公平(LIFO)策略，非公平侧罗会导致一些数据永远无法被消费的情况？
使用SynchronousQueue阻塞队列一般要求maximumPoolSizes为无界(Integer.MAX_VALUE)，避免线程拒绝执行操作。


LinkedBlockingQueue是一个无界缓存等待队列。当前执行的线程数量达到corePoolSize的数量时，剩余的元素会在阻塞队列里等待。
    （所以在使用此阻塞队列时maximumPoolSizes就相当于无效了），每个线程完全独立于其他线程。
    生产者和消费者使用独立的锁来控制数据的同步，即在高并发的情况下可以并行操作队列中的数据。


https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html
线程池的参数并不好配置。一方面线程池的运行机制不是很好理解，配置合理需要强依赖开发人员的个人经验和知识；
    另一方面，线程池执行的情况和任务类型相关性较大，IO密集型和CPU密集型的任务运行起来的情况差异非常大
    
事故描述：XX页面展示接口产生大量调用降级，数量级在几十到上百。
事故原因：该服务展示接口内部逻辑使用线程池做并行计算，由于没有预估好调用的流量，导致最大核心数设置偏小，
    大量抛出RejectedExecutionException，触发接口降级条件，示意图如下：   

2018年XX业务服务不可用S2级故障
事故描述：XX业务提供的服务执行时间过长，作为上游服务整体超时，大量下游服务调用失败。
事故原因：该服务处理请求内部逻辑使用线程池做资源隔离，由于队列设置过长，最大线程数设置失效，导致请求数量增加时，
    大量任务堆积在队列中，任务执行时间过长，最终导致下游服务的大量调用超时失败。
    
    
JDK允许线程池使用方通过ThreadPoolExecutor的实例来动态设置线程池的核心策略，
    以setCorePoolSize为方法例，在运行期线程池使用方调用此方法设置corePoolSize之后，
        线程池会直接覆盖原来的corePoolSize值，并且基于当前值和原始值的比较结果采取不同的处理策略。
    对于当前值小于当前工作线程数的情况，说明有多余的worker线程，此时会向当前idle的worker线程发起中断请求以实现回收，
        多余的worker在下次idel的时候也会被回收；对于当前值大于原始值且当前队列中有待执行任务，
        则线程池会创建新的worker线程来执行队列任务，setCorePoolSize具体流程如下：    
    