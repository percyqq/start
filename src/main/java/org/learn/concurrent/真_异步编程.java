package org.learn.concurrent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * https://developer.ibm.com/zh/articles/j-cf-of-jdk8/
 * @create: 2020-08-11 15:43
 */
public class 真_异步编程 {

    private static String printThreadTime(String... data) {
        StringBuilder sb = new StringBuilder(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
        sb.append(", ");
        sb.append(Thread.currentThread().getName());
        sb.append(", -> ");
        for (String str : data) {
            sb.append(str);
        }
        return sb.toString();
    }

    private static String[] GANKER = {"piss", "zsmj"};

    public static void main(String[] args) {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 5, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));

        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(printThreadTime("exec... ganker 开始蹲点抓人"));
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int pos = (int) (new Date().getTime() % 2);
            return GANKER[pos];
        }, executor);

        String data = "";
        try {
            data = completableFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        CompletableFuture.completedFuture(data).thenApplyAsync(s -> {
            String ret = printThreadTime(s, "，来gank了");
            System.out.println(ret);

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (GANKER[0].equals(s)) {
                throw new RuntimeException("被对方蹲人抓住，gank 失败");
            }
            return "抓住对方Farm一套带走，抓人成功。";
        }).handleAsync((returnStr, exception) -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String ret;
            if (exception == null) {
                ret = printThreadTime(returnStr);
                System.out.println(ret);
            } else {
                ret = printThreadTime("蹲人失败");
                //exception.printStackTrace();
                System.err.println(ret);
            }
            return "【" + ret + "】";
        }).thenApplyAsync((returnStr) -> {
            String ret = printThreadTime("all done, ", returnStr);
            System.out.println(ret);
            return returnStr;
        });

        System.out.println(printThreadTime("-蹲人的时候，无聊抽杆烟"));
//        try {
//            Thread.currentThread().join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    public static void main1(String[] args) {
        Supplier<String> stringSupplier = () -> {
            System.out.println(printThreadTime("exec... 开始蹲人"));
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "抓住对方ganker一套带走，蹲人成功。";
        };
        CompletableFuture<String> completableFuture =
                CompletableFuture.supplyAsync(stringSupplier);

        completableFuture.whenComplete((returnStr, exception) -> {
            if (exception == null) {
                System.out.println(printThreadTime(returnStr));
            } else {
                System.out.println(printThreadTime("蹲人失败"));
                exception.printStackTrace();
            }
        });

        System.out.println(printThreadTime("-蹲人的时候，无聊抽杆烟"));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
