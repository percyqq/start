package org.learn.algorithm.easy;

import java.math.BigDecimal;

/**
 * @description:
 * @create: 2020-12-22 15:50
 */
public class 无限逼近求平方根 {

    public static void main(String[] args) {

        double d = 1e-5;
        System.out.println(new BigDecimal(d));
        float ret = sqrt(2);
        System.out.println(ret);
    }

    private static float sqrt(float a) {
        float x0 = a / 2;
        float x1 = (x0 + a / x0) / 2;
        do {
            x0 = x1;
            x1 = (x0 + a / x0) / 2;
        }
        while (Math.abs(x1 - x0) >= 1e-5);

        return x1;
    }
}
