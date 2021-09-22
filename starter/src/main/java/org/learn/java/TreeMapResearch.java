package org.learn.java;

import lombok.Data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * @description:
 * @create: 2020-11-04 09:52
 */
public class TreeMapResearch {

    @Data
    static class Info implements Comparable<Info> {
        private Integer id;
        private String name;

        @Override
        public int compareTo(Info o) {
            return Integer.compare(this.id, o.id);
            //return Comparator.comparing(Info::getId).compare(this, o);
        }
    }

    public static void main(String[] args) {

        TreeMap<Info, String> d = new TreeMap<>();
        Info l1 = new Info();
        l1.id = 1;
        d.put(l1, "1");

        Info l2 = new Info();
        l2.id = 0;
        d.put(l2, "2");

        System.out.println(d);
        HashMap G;

    }
}
