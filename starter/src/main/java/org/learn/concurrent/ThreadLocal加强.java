package org.learn.concurrent;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocal加强 {


    public static void main(String[] args) {

        TransmittableThreadLocal d;

        AtomicInteger integer = new AtomicInteger(1);

        wtf:
        for (; ; ) {
            System.out.println("loop " + integer.get());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (integer.getAndIncrement() == 11) {
                System.out.println("end ");
                break wtf;
            }
            if (integer.get() % 1 == 0) {
                System.out.println(" continue : " + integer.get());
                continue wtf;
            }
        }


        System.out.println("666");
    }


}
