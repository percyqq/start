package org.learn.java;

import java.util.ArrayList;

/**
 * @description:
 * @author: qingqing
 * @create: 2020-08-12 12:36
 */
public class 泛型 {
    public static void main(String[] args) {

        ArrayList list2 = new ArrayList<String>();
        list2.add("1"); //编译通过
        list2.add(2); //编译通过

        Object object = list2.get(0); //返回类型就是Object
        System.out.println(object);

        Object objec1 = list2.get(1); //返回类型就是Object
        System.out.println(objec1);

    }
}
