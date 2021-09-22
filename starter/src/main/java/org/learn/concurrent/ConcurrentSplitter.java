package org.learn.concurrent;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @date: 2019/12/16
 * @description: 大批量数据拆分器
 */
@Slf4j
public class ConcurrentSplitter {

    public interface SplitCallable<T, R> {
        List<R> run(List<T> group);
    }

    public interface SplitRunnable<T> {
        void run(List<T> group);
    }

    /**
     * ForkJoin线程池，有分组大小，有结果返回
     */
    public static <T, R> List<R> split(List<T> list, int groupSize, SplitCallable<T, R> splitCallable) {
        return split((PlatformTransactionManager) null, list, groupSize, splitCallable);
    }

    /**
     * ForkJoin线程池，有分组大小，有结果返回 -- 有事务
     */
    public static <T, R> List<R> split(PlatformTransactionManager transactionManager, List<T> list, int groupSize, SplitCallable<T, R> splitCallable) {
        List<List<T>> groupList = Lists.partition(list, groupSize);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(groupList.size());
        AtomicBoolean flag = new AtomicBoolean(true);
        Map<String, String> webThreadContext = MDC.getCopyOfContextMap();
        List<R> rlist = groupList.parallelStream()
                .map(group -> {
                    MDC.setContextMap(webThreadContext);
                    if (transactionManager != null) {
                        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                        TransactionStatus status = transactionManager.getTransaction(def);
                        try {
                            splitCallable.run(group);
                        } catch (Exception e) {
                            log.error("task run error \n", e);
                            flag.compareAndSet(true, false);
                        }

                        try {
                            cyclicBarrier.await(180, TimeUnit.SECONDS);
                            if (flag.get()) {
                                transactionManager.commit(status);
                            } else {
                                status.setRollbackOnly();
                                transactionManager.rollback(status);
                            }
                        } catch (Exception e) {
                            log.error("task await error \n", e);
                            status.setRollbackOnly();
                            transactionManager.rollback(status);
                        }
                        return null;
                    } else {
                        MDC.clear();
                        return splitCallable.run(group);
                    }
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
        if (!flag.get()) {
            throw new RuntimeException("split error");
        }
        return rlist;
    }

    /**
     * 有自定义执行线程，有分组大小，超时60s,有结果返回 -- 有事务
     */
    public static <T, R> List<R> split(ExecutorService executorService, List<T> list, int groupSize, SplitCallable<T, R> splitCallable) {
        return split(null, executorService, list, groupSize, 60, splitCallable);
    }

    public static void main(String[] args) {
        int size = 3;

        CyclicBarrier cyclicBarrier = new CyclicBarrier(size);
        for (int i = 0; i < size; i++) {
            int finalI = i;
            new Thread(() -> {

                if (finalI == 0) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println(Thread.currentThread().getName() + " : ");
                try {
                    cyclicBarrier.await(5, TimeUnit.SECONDS);
                    String s = null;
                    s.toString();
                } catch (Exception e) {
                    log.error(Thread.currentThread().getName() + " , task await error \n", e);
                }

//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (BrokenBarrierException e) {
//                    e.printStackTrace();
//                } catch (TimeoutException e) {
//                    e.printStackTrace();
//                }

            }).start();
        }
    }

    /**
     * 有自定义执行线程，有分组大小，有超时,有结果返回 -- 有事务
     */
    public static <T, R> List<R> split(PlatformTransactionManager transactionManager, ExecutorService executorService, List<T> list, int groupSize, long timeout, SplitCallable<T, R> splitCallable) {
        List<List<T>> groupList = Lists.partition(list, groupSize);
        List<Future> futures = new ArrayList<>();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(groupList.size());
        AtomicBoolean flag = new AtomicBoolean(true);
        Map<String, String> webThreadContext = MDC.getCopyOfContextMap();
        for (List<T> group : groupList) {
            Future f = executorService.submit(() -> {
                MDC.setContextMap(webThreadContext);
                if (transactionManager != null) {
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    TransactionStatus status = transactionManager.getTransaction(def);
                    List<R> rList1 = null;
                    try {
                        rList1 = splitCallable.run(group);
                    } catch (Exception e) {
                        log.error("task run error \n", e);
                        flag.compareAndSet(true, false);
                    }

                    try {
                        cyclicBarrier.await(timeout, TimeUnit.SECONDS);
                        if (flag.get()) {
                            transactionManager.commit(status);
                        } else {
                            status.setRollbackOnly();
                            transactionManager.rollback(status);
                        }
                    } catch (Exception e) {
                        log.error("task await error \n", e);
                        status.setRollbackOnly();
                        transactionManager.rollback(status);
                    }
                    return rList1;
                } else {
                    MDC.clear();
                    return splitCallable.run(group);
                }
            });
            futures.add(f);
        }
        try {
            List<R> resultList = Lists.newArrayList();
            for (Future f : futures) {
                resultList.addAll((Collection<? extends R>) f.get(timeout, TimeUnit.SECONDS));
            }
            if (!flag.get()) {
                throw new RuntimeException("split error");
            }
            return resultList;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ForkJoin线程池，有分组大小
     */
    public static <T> void split(List<T> list, int groupSize, SplitRunnable<T> splitRunnable) {
        List<List<T>> groupList = Lists.partition(list, groupSize);
        groupList.parallelStream().forEach(splitRunnable::run);
    }

    /**
     * ForkJoin线程池，有分组大小 -- 有事务
     */
    public static <T> void split(PlatformTransactionManager transactionManager, List<T> list, int groupSize, SplitRunnable<T> splitRunnable) {
        List<List<T>> groupList = Lists.partition(list, groupSize);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(groupList.size());
        AtomicBoolean flag = new AtomicBoolean(true);
        Map<String, String> webThreadContext = MDC.getCopyOfContextMap();
        groupList.parallelStream().forEach(group -> {
            MDC.setContextMap(webThreadContext);
            if (transactionManager != null) {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus status = transactionManager.getTransaction(def);
                try {
                    splitRunnable.run(group);
                } catch (Exception e) {
                    log.error("task run error \n", e);
                    flag.compareAndSet(true, false);
                }

                try {
                    cyclicBarrier.await(180, TimeUnit.SECONDS);
                    if (flag.get()) {
                        transactionManager.commit(status);
                    } else {
                        status.setRollbackOnly();
                        transactionManager.rollback(status);
                    }
                } catch (Exception e) {
                    log.error("task await error \n", e);
                    status.setRollbackOnly();
                    transactionManager.rollback(status);
                }
            } else {
                splitRunnable.run(group);
            }
            MDC.clear();
        });
        if (!flag.get()) {
            throw new RuntimeException("split error");
        }
    }

    /**
     * 有自定义执行线程，有分组大小，默认超时60s
     */
    public static <T> void split(ExecutorService executorService, List<T> list, int groupSize, SplitRunnable<T> splitRunnable) {
        split(executorService, list, groupSize, 60, splitRunnable);
    }

    /**
     * 有自定义执行线程，有分组大小，默认超时60s -- 有事务
     */
    public static <T> void split(PlatformTransactionManager transactionManager, ExecutorService executorService, List<T> list, int groupSize, SplitRunnable<T> splitRunnable) {
        split(transactionManager, executorService, list, groupSize, 60, splitRunnable);
    }

    /**
     * 有自定义执行线程，有分组大小，有超时
     */
    public static <T> void split(ExecutorService executorService, List<T> list, int groupSize, long timeout, SplitRunnable<T> splitRunnable) {
        split(null, executorService, list, groupSize, timeout, splitRunnable);
    }

    /**
     * 有自定义执行线程，有分组大小，有超时 -- 有事务
     */
    public static <T> void split(PlatformTransactionManager transactionManager, ExecutorService executorService, List<T> list, int groupSize, long timeout, SplitRunnable<T> splitRunnable) {
        List<List<T>> groupList = Lists.partition(list, groupSize);
        List<Future> futures = new ArrayList<>();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(groupList.size());
        AtomicBoolean flag = new AtomicBoolean(true);
        Map<String, String> webThreadContext = MDC.getCopyOfContextMap();
        for (List<T> group : groupList) {
            Future f = executorService.submit(() -> {
                MDC.setContextMap(webThreadContext);
                if (transactionManager != null) {
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    TransactionStatus status = transactionManager.getTransaction(def);
                    try {
                        splitRunnable.run(group);
                    } catch (Exception e) {
                        log.error("task run error \n", e);
                        flag.compareAndSet(true, false);
                    }

                    try {
                        cyclicBarrier.await(timeout, TimeUnit.SECONDS);
                        if (flag.get()) {
                            transactionManager.commit(status);
                        } else {
                            status.setRollbackOnly();
                            transactionManager.rollback(status);
                        }
                    } catch (Exception e) {
                        log.error("task await error \n", e);
                        status.setRollbackOnly();
                        transactionManager.rollback(status);
                    }
                } else {
                    splitRunnable.run(group);
                }
                MDC.clear();
            });
            futures.add(f);
        }
        try {
            for (Future f : futures) {
                f.get(timeout, TimeUnit.SECONDS);
            }
            if (!flag.get()) {
                throw new RuntimeException("split error");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
