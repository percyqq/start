package org.learn.concurrent;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @date: 2019/12/19
 * @description: 多个任务聚合并发执行
 */
public class ConcurrentJoiner {

    public interface JoinCallable<V> {
        String key();
        V call();
    }


    public static <R> void combine(Callable<R>... callables) {
        Lists.newArrayList(callables).parallelStream().collect(Collectors.toMap(Callable::hashCode, call -> call)).entrySet()
                .parallelStream().map(entry -> {
            try {
                return new AbstractMap.SimpleEntry<String, Object>(entry.getKey() + "", entry.getValue().call());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <R> void combine(ExecutorService executorService, Callable<R>... callables) {
        combine(executorService, 60, callables);
    }

    public static <R> void combine(ExecutorService executorService, long timeout, Callable<R>... callables) {
        List<Future> futures = new ArrayList<>();
        for (Callable<R> callable : callables) {
            Future f = executorService.submit(callable::call);
            futures.add(f);
        }
        try {
            for (Future f : futures) {
                f.get(timeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> Map<String, Object> combine(JoinCallable<R>... callables) {
        return Lists.newArrayList(callables).stream().collect(Collectors.toMap(JoinCallable::key, call -> call)).entrySet()
                .parallelStream().map(entry ->
                        new AbstractMap.SimpleEntry<String, Object>(entry.getKey(), entry.getValue().call()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <R> Map<String, Object> combine(ExecutorService executorService, JoinCallable<R>... callables) {
        return combine(executorService, 60, callables);
    }

    public static <R> Map<String, Object> combine(ExecutorService executorService, long timeout, JoinCallable<R>... callables) {
        Map<String, Future> futures = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        for (JoinCallable<R> callable : callables) {
            Future f = executorService.submit(callable::call);
            futures.put(callable.key(), f);
        }
        try {
            for (Map.Entry<String, Future> entry : futures.entrySet()) {
                resultMap.put(entry.getKey(), entry.getValue().get(timeout, TimeUnit.SECONDS));
            }
            return resultMap;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

}
