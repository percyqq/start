package org.learn.concurrent;

import java.util.concurrent.TimeUnit;

public class Volatile的 {

    private static boolean flag = false;

    private static int i = 0;

    private static volatile int ix = 0;

    private static volatile Integer ixF = 0;

    //  由于变量 flag 没有被 volatile 修饰，而且在子线程休眠的 100ms 中， while 循环的 flag 一直为 false，
    //      循环到一定次数后，触发了 jvm 的即时编译功能，进行循环表达式外提（Loop Expression Hoisting），导致形成死循环。
    //          而如果加了 volatile 去修饰 flag 变量，保证了 flag 的可见性，则不会进行提升。

    //  https://mp.weixin.qq.com/s/qYKBJPrwliXiKfX7A1wQPg
    //      从  https://mp.weixin.qq.com/s/cONJTZbzjDVPcDIRa-bfdQ 调到上面的文章
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                flag = true;

                System.out.println("flag set to true !");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        //v1();  v1()中的方案1，方案2，加上都可以结束
        //v2();     //v2()， v3()中结束时，i达到了比较大的值.. 338724933
        //v3();

        //-XX:+UnlockDiagnosticVMOptions   -XX:+PrintAssembly -XX:CompileCommand=dontinline,*VolatileExample.main -XX:CompileCommand=compileonly,*VolatileExample.main
        //研究一下v1()中 的方案1
        while (!flag) {
            i++;

            //同步方法可以防止在循环期间缓存 pizzaArrived（就是我们的stop）。
            //System.out.println("修改flag标识 ==> " + flag);

            try {
                TimeUnit.MILLISECONDS.sleep(222);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("end, i = " + i);

    }

    private static void answer() {
        //运行的时候配置下面的参数，其含义是禁止 JIT 编译器的加载：
        //-Djava.compiler=NONE

        // 优化 from
        int i = 0;
        while (!flag) {
            i++;
        }

        //to
        if (!flag) {
            while (true) {
                i++;
            }
        }


    }

    private static void v3() {
        while (!flag) {
            ixF++;
        }
        System.out.println("end, ixF = " + ixF);
    }

    private static void v2() {
        while (!flag) {
            ix++;
        }
        System.out.println("end, ix = " + ix);
    }


    private static void v1() {
        while (!flag) {
            i++;

            //方案1.  加上下面的打印
            //System.out.println("modify flag ==> " + flag);

            //方案2.  sleep几秒
            try {
                TimeUnit.MILLISECONDS.sleep(222);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("end, i = " + i);
    }
}
