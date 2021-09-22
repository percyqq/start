package org.learn.java;

/**
 * @create: 2020-08-17 23:54
 */
public class 内存分配 {

    public static void main(String[] args) {

        //VM 参数：  -Xmx20m -Xms5m -XX:+PrintGCDetails
        System.out.println("Xmx=" + Runtime.getRuntime().maxMemory() / 1024.0 / 1024 + "M");    //系统的最大空间
        System.out.println("free mem=" + Runtime.getRuntime().freeMemory() / 1024.0 / 1024 + "M");  //系统的空闲空间
        System.out.println("total mem=" + Runtime.getRuntime().totalMemory() / 1024.0 / 1024 + "M");  //当前可用的总空间
    }
}
