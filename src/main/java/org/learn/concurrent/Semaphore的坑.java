package org.learn.concurrent;


import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * @description:
 * @create: 2020-08-05 09:49
 */
public class Semaphore的坑 {

    //  https://mp.weixin.qq.com/s/0J1bbgCqQWqNlAzbnhI_bQ


    public static void main(String[] args) throws InterruptedException {

        Integer parkSpace = 3;
        System.out.println("这里有" + parkSpace + "个停车位,先到先得啊！");
        Semaphore semaphore = new Semaphore(parkSpace, true);


        Thread threadA = new Thread(new ParkCar(1, "布加迪", semaphore), "赵四");
        Thread threadB = new Thread(new ParkCar(2, "法拉利", semaphore), "刘能、谢广坤");
        Thread threadC = new Thread(new ParkCar(1, "劳斯莱斯", semaphore), "why哥");

        threadA.start();
        threadB.start();
        threadC.start();


        xx();
        //DispatcherServlet 都是;
        Collections.sort(null);
        Arrays.sort(new Object[]{});

        Executors.newCachedThreadPool();
        Executors.newFixedThreadPool(5);
        Executors.newSingleThreadExecutor();
        Executors.newWorkStealingPool();
    }

    private static void xx() {
        new Thread(() -> {
            try {
                try {
                    System.out.println("00");
                    throw new RuntimeException();
                } catch (Exception e) {
                    System.out.println(22);
                    return;
                }
            } finally {
                System.out.println(11);
            }
        }).start();
    }

    // 方案2
    static class MySemaphore extends Semaphore {

        public MySemaphore(int permits) {
            super(permits);
        }

        //https://mp.weixin.qq.com/s/0J1bbgCqQWqNlAzbnhI_bQ
        // 为什么使用线程安全队列。==> MySemaphore 由多个线程操作，当前队列管理操作。
        private final ConcurrentLinkedQueue<Thread> queue = new ConcurrentLinkedQueue<>();

        @Override
        public boolean tryAcquire() {
            boolean ret = super.tryAcquire();
            if (ret) {
                this.queue.add(Thread.currentThread());
            }
            return ret;
        }

        @Override
        public void release(int pemits) {
            final Thread currentThread = Thread.currentThread();

            if (!this.queue.contains(currentThread)) {
                return;
            }

            super.release(pemits);

            this.queue.remove(currentThread);
        }
    }

    static class ParkCar implements Runnable {
        private int n;
        private String carName;
        private Semaphore semaphore;

        public ParkCar(int n, String carName, Semaphore semaphore) {
            this.n = n;
            this.carName = carName;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            if (semaphore.availablePermits() < n) {
                System.out.println(Thread.currentThread().getName() + "来停车,但是停车位不够了,等着吧");
            }

            // 方案1
            try {
                semaphore.acquire(n);
            } catch (InterruptedException e) {
                //这里一定要try/catch， 不然走了finally就会导致多了几个[许可证]
                e.printStackTrace();
                return;
            }

            try {
                System.out.println(Thread.currentThread().getName() + "把自己的" + carName + "停进来了,剩余停车位:"
                        + semaphore.availablePermits() + "辆");

                //模拟停车时长
                int parkTime = ThreadLocalRandom.current().nextInt(1, 6);
                TimeUnit.SECONDS.sleep(parkTime);
                System.out.println(Thread.currentThread().getName() + "把自己的" + carName + "开走了,停了" + parkTime + "小时");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                semaphore.release(n);
                System.out.println(Thread.currentThread().getName() + "走后,剩余停车位:" + semaphore.availablePermits() + "辆");
            }
        }
    }
}
