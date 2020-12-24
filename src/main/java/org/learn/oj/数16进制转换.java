package org.learn.oj;

import java.math.BigInteger;
import java.util.Scanner;

/**
 * @description:
 * @create: 2020-10-20 23:18
 */
public class 数16进制转换 {


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
//        while (in.hasNextInt()) {
//            //注意while处理多个case
//            int a = in.nextInt();
//            int b = in.nextInt();
//            System.out.println(a + b);
//        }
        while (in.hasNextLine()) {
            String se = in.nextLine();
            System.out.println(se);

        }

        BigInteger b = new BigInteger("135", 10);
        String ret = b.toString(16).toUpperCase();
        System.out.println(ret);

        Integer.parseInt("5", 16);
    }
}
