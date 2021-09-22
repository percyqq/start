package org.learn.code;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @create: 2021-01-07 16:48
 */
public class StreamUse {

    @Data
    @AllArgsConstructor
    static class U {
        int id;
        String name;
        String desc;
    }

    public static void main(String[] args) {
        List<U> l = new ArrayList<>();
        l.add(new U(1, "2", "1"));
        l.add(new U(1, "2", "2"));
        l.add(new U(1, "2", "3x"));
        l.add(new U(1, "2", "3"));
        l.add(new U(1, "2", "3D"));


        l.add(new U(2, "3", "6"));
        l.add(new U(2, "3", "6"));
        l.add(new U(2, "4", "61"));
        l.add(new U(3,      "4", "62"));
        l.add(new U(3, "3", "6"));

        Map<Integer, List<String>> mmp = l.stream().collect(
                Collectors.groupingBy(U::getId, Collectors.mapping(U::getName, Collectors.toList()))
        );

        System.out.println(mmp);
    }


}