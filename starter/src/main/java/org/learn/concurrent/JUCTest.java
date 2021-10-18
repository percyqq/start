package org.learn.concurrent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class JUCTest {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss.sss");
    Supplier<Runnable> s = () -> () -> System.out.println("wtf");
    Supplier<Runnable> b = new Supplier<Runnable>() {
        @Override
        public Runnable get() {
            return new Runnable() {
                @Override
                public void run() {
                    System.out.println("wtf");
                }
            };
        }
    };

    public static void main(String[] args) {
        Supplier<Runnable> xx = () -> () -> {
            System.out.println(Thread.currentThread().getName() + " ===== start at " + sdf.format(new Date()));
        };
        Stream.generate(xx).limit(5).forEach(CompletableFuture::runAsync);
    }

    public static void main1(String[] args) throws InterruptedException {
        AtomicReference<FutureTask<Integer>> a = new AtomicReference<>();
        Runnable task = () -> {
            while (true) {
                FutureTask<Integer> f = new FutureTask<>(() -> 1);
                a.set(f);
                f.run();
            }
        };

        Supplier<Runnable> observe = () -> () -> {
            System.out.println(Thread.currentThread().getName() + " ===== start at " + new Date());
            while (a.get() == null) {
                System.out.println(Thread.currentThread().getName() + " ===== nothing to do, wait reference get value ");
            }


            int c = 0, ic = 0;
            while (true) {
                c++;
                FutureTask<Integer> f = a.get();
                while (!f.isDone()) {
                }

                try {
                    /*
                    Set the interrupt flag of this thread.
                    The future reports it is done but in some cases a call to
                    "get" will result in an underlying call to "awaitDone" if
                    the state is observed to be completing.
                    "awaitDone" checks if the thread is interrupted and if so
                    throws an InterruptedException.
                     */
                    Thread.currentThread().interrupt();
                    f.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    ic++;
                    System.out.println("InterruptedException observed when isDone() == true " + c + " " + ic + " " + Thread.currentThread());
                }
            }
        };


        CompletableFuture.runAsync(task);
        Supplier<Runnable> d = observe::get;
        int cnt = Runtime.getRuntime().availableProcessors() - 1;
        System.out.println(" =====>>>>>  " + cnt + " , " + d);
        Stream.generate(d)
                .limit(cnt)
                .forEach(CompletableFuture::runAsync);

        Thread.sleep(1000);
        System.exit(0);

    }
}
