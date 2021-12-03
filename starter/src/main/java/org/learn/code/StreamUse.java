package org.learn.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.function.Function;
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
        l.add(new U(3, "4", "62"));
        l.add(new U(3, "3", "6"));

        Map<Integer, List<String>> mmp = l.stream().collect(
                Collectors.groupingBy(U::getId, Collectors.mapping(U::getName, Collectors.toList()))
        );

        //分组同时还转换
        Function<U, U1> function = u -> {
            U1 u1 = new U1();
            BeanUtils.copyProperties(u, u1);
            return u1;
        };

        List<U> users = l;
        Map<Integer, List<U1>> userMap = users.stream().collect(
                Collectors.groupingBy(U::getId, Collectors.mapping(function, Collectors.toList()))
        );
        System.out.println(" userMap ==> " + userMap);


        // 打平取出所有U，返回List
        Collection<List<U1>> cu = userMap.values();
        List<U1> w666 = cu.stream().flatMap(Collection::stream).collect(Collectors.toList());
        System.out.println(" w666 ==> " + w666);

        System.out.println(mmp);
    }


    @Data
    static class U1 {
        int id;
        String name;
        String desc;
    }


}