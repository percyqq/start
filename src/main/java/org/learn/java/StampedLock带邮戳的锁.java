package org.learn.java;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

/**
 * @create: 2020-07-30 16:46
 */
public class StampedLock带邮戳的锁 {
    private static StampedLock lock = new StampedLock();
    private static List<String> data = new ArrayList<>();

    // 悲观写
    public static void write() {
        long stamped = -1l;
        try {
            stamped = lock.writeLock();
            data.add("写入数据:" + stamped);
            System.out.println("写入 数据是:" + stamped);
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {

        } finally {

        }

    }

    ThreadPoolExecutor executor;

}
