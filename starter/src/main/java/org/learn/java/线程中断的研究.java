package org.learn.java;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * @description:
 * @create: 2020-12-22 21:24
 */
public class 线程中断的研究 {

    public static void main(String[] args) {

        CountDownLatch countDownLatch = new CountDownLatch(1);


    }


    //这是 jdk8 的 AbstractQueuedSynchronizer
    // Question ： 如果是中断触发解除线程阻塞状态，为什么这里需要把中断复位后再后面二次中断传递，
    //  难道直接使用isInterrupted()不香吗，都不需要二次触发中断？

    // Answer :  如果使用isInterrupted，没有复位中断状态。那么中断触发解除线程阻塞状态后，
    //  到外层循环里如果再次获取锁失败的情况下，又会再次进入到parkAndCheckInterrupt方法后，
    //      LockSupport.park(this) 不会阻塞中断状态下的线程。从而导致死循环。所以需要获取中断状态位后复位。

    // AQS本身是没有实现 tryAcquire 方法的.
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Acquires in exclusive mode, ignoring interrupts.  Implemented
     * by invoking at least once {@link #tryAcquire},
     * returning on success.  Otherwise the thread is queued, possibly
     * repeatedly blocking and unblocking, invoking {@link#tryAcquire} until success.  This method can be used
     * to implement method {@link Lock#lock}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *              #tryAcquire} but is otherwise uninterpreted and
     *            can represent anything you like.
     */
    // ===>  tryAcquire 是子类的实现
    public final void acquire(int arg) {
        //if (!tryAcquire(arg) &&
        //        acquireQueued(addWaiter(AbstractQueuedSynchronizer.Node.EXCLUSIVE), arg))
            // !!!!! Q:  这里是二次中断，我的理解是如果使用isInterrupted()，那么这里完全不需要再执行一次中断呀
            //  如果线程本身是中断状态，为什么要清理一遍之后，再重新触发一遍呢？
            selfInterrupt();

        //  下面的acquireQueued()方法，内部调用了 parkAndCheckInterrupt()
        //  最内层方法里面使用Thread.interrupted()获取线程中断状态后，随即清除了中断标记位，然后把获得的中断标记传递到外层方法中，
        //      逻辑判断后又进行了一次中断标记Thread.currentThread().interrupt()。感觉很多余呀
    }

    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }


    /**
     * Convenience method to park and then check if interrupted
     *
     * @return {@code true} if interrupted
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }

    /*
    final boolean acquireQueued(final AbstractQueuedSynchronizer.Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (; ; ) {
                final AbstractQueuedSynchronizer.Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    ======>   parkAndCheckInterrupt()) //!!!!

                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
    */


    /**
     * Acquires in exclusive interruptible mode.
     * @param arg the acquire argument
     */
    /*
    private void doAcquireInterruptibly(int arg)
            throws InterruptedException {
        final AbstractQueuedSynchronizer.Node node = addWaiter(AbstractQueuedSynchronizer.Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final AbstractQueuedSynchronizer.Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                        parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
    */
}
