package org.learn.concurrent;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@ToString
public class 线程池动态调整 {
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

    // runState is stored in the high-order bits
    private static final int RUNNING    = -1 << COUNT_BITS;
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;


    public static void main(String[] args) throws InterruptedException {
        System.out.println(线程池动态调整.CAPACITY);

        Collection d;
        LinkedBlockingQueue blockingQueue = new LinkedBlockingQueue(5);
        // ampq -client : VariableLinkedBlockingQueue d;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("put(" + i + ")");
                try {
                    blockingQueue.put(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("LinkedBlockingQueue put(" + i + ")成功，剩余容量：" + blockingQueue.remainingCapacity());
            }
        });

        TimeUnit.SECONDS.sleep(3);





    }

    private static void test1() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10), new NamedThreadFactory("碉堡了"));

        for (int i = 0; i < 15; i++) {
            executor.submit(() -> {
                threadPoolStatus("创建任务", executor);
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        threadPoolStatus("改变之前", executor);
        executor.setCorePoolSize(10);
        //executor.setMaximumPoolSize(10);

        //todo
        executor.prestartAllCoreThreads();

        threadPoolStatus("改变之后", executor);

        Thread.currentThread().join();
    }

    private static void threadPoolStatus(String name, ThreadPoolExecutor executor) {
        LinkedBlockingQueue queue = (LinkedBlockingQueue) executor.getQueue();
        System.out.println(new Date() + ", " +
                Thread.currentThread().getName() + "-" + name + "-:" +
                "核心线程数：" + executor.getCorePoolSize() +
                " 活动线程数：" + executor.getActiveCount() +
                " 最大线程数：" + executor.getMaximumPoolSize() +
                " 线程池或与哦度：" + (executor.getActiveCount() / executor.getMaximumPoolSize() * 100) +
                " 任务完成数：" + executor.getCompletedTaskCount() +
                " 队列大小:" + (queue.size() + queue.remainingCapacity()) +
                " 当前排队线程数：" + queue.size() +
                " 队列剩余大小：" + queue.remainingCapacity() +
                " 队列使用度:" + (queue.size() / (queue.size() + queue.remainingCapacity()) * 100)
        );
    }
}


class NamedThreadFactory implements ThreadFactory {
    private static final Logger logger = LoggerFactory.getLogger(NamedThreadFactory.class);
    private final String name;
    private final boolean daemon;
    private final ThreadGroup group;
    private final AtomicInteger threadNumber;
    static final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (t, e) -> {
        if (!(e instanceof InterruptedException) && (e.getCause() == null || !(e.getCause() instanceof InterruptedException))) {
            logger.error("from " + t.getName(), e);
        }
    };

    public NamedThreadFactory(String name) {
        this(name, true);
    }

    public NamedThreadFactory(String name, boolean daemon) {
        this.threadNumber = new AtomicInteger(0);
        this.name = name;
        this.daemon = daemon;
        SecurityManager s = System.getSecurityManager();
        this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(this.group, r, "[" + this.name + "-" + this.threadNumber.getAndIncrement() + "]", 0L);
        t.setDaemon(this.daemon);
        if (t.getPriority() != 5) {
            t.setPriority(5);
        }

        t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return t;
    }
}