package org.learn.concurrent;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @create: 2020-07-24 13:51
 * <p>
 * https://www.jianshu.com/p/39a25b3960df
 */
public class CompletableFutureExample {

    private static final String DATE_FORMAT = "mm-dd HH:mm:ss.SSS";

    private static String getDateStr() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    public static void main(String[] args) {

        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(getDateStr() + ", finally，task1返回值来了，it's ==> [wtf 135]");
            return "[wtf 135]";
        });


        CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(getDateStr() + ", task2 执行完成了");
        });


        CompletableFuture<String> task3 = task2.thenCombine(task1, (voidObj, stringObj) -> {
            System.out.println(getDateStr() + ", task3 in, voidObj : " + voidObj + ", stringObj : " + stringObj);

            //触发异常流程
            System.out.println(1/0);

            System.out.println(getDateStr() + ", task3 end with success!");
            return stringObj;
        }).exceptionally(e -> {
            System.out.println(getDateStr() + ", wtf, task3 end with fail!");
            return "fail, loser";
        });

        String ss = task3.join();
        System.out.println(getDateStr() + ", task3 : " + ss);


    }


    public static void main1(String[] args) {
        Integer s[] = new Integer[]{1, 2, 3, 4, 5, 6};
        List<CompletableFuture<String>> futures = Arrays.stream(s).map(i -> {
            int sss = i;
            return CompletableFuture.supplyAsync(() -> {
                int time = new Random().nextInt(10);
                System.out.println(Thread.currentThread().getName() + " start at " + new Date() + " , will exec time : " + time + "s");
                try {
                    TimeUnit.SECONDS.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + " end at " + new Date());
                return sss + " enddd";
            });
        }).collect(Collectors.toList());

        System.out.println(Thread.currentThread().getName() + " ====> start, at time : " + new Date());
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
        allOf.whenComplete((futureVal, throwable) -> {
            System.out.println(Thread.currentThread().getName() + " ====> whenComplete at time : " + new Date());
            futures.forEach(f -> {
                String vv = f.getNow(null);
                System.out.println(vv);
            });
            System.out.println(Thread.currentThread().getName() + " ====> get future at time : " + new Date());
        }).exceptionally(e -> {
            e.printStackTrace();
            System.out.println(Thread.currentThread().getName() + " ====> error whenComplete at time : " + new Date());
            futures.forEach(f -> {
                String vv = f.getNow(null);
                System.out.println(vv);
            });
            System.out.println(Thread.currentThread().getName() + " ====> error get future at time : " + new Date());

            return null;
        });
        allOf.join();
    }
}
