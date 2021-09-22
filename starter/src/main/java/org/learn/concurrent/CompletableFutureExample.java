package org.learn.concurrent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
}
