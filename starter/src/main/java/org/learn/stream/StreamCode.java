package org.learn.stream;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @create: 2020-07-24 13:54
 */
public class StreamCode {

    // java8  stream
    static Map<String, String> pubData = new HashMap<>();

    static {
        pubData.put("1", "111");
        pubData.put("2", "222");
        pubData.put("3", "333");
        pubData.put("4", "444");
        pubData.put("5", "555");
    }

    // 1,3,5,7,9 拆分层size : 2的  List<List>，分批次执行。
    // StreamCode::genData  ===>  根据每批次的数据，查询过滤，比如存在交集的才取出来
    public static void main(String[] args) {
        List<String> data = Lists.newArrayList("1", "3", "5", "7", "9");
        Spliterator<List<String>> spliterator = Iterables.partition(data, 2).spliterator();
        Map<String, String> kkk = StreamSupport.stream(spliterator, false).map(StreamCode::genData)
                .reduce(new HashMap<>(), (retMap, loopDataMap) -> {
                    retMap.putAll(loopDataMap);
                    return retMap;
                });
        System.out.println(kkk);
    }

    private static Map<String, String> genData(List<String> ds) {
        Map<String, String> ret = new HashMap<>();
        ds.forEach(item -> {
            String val = pubData.get(item);
            if (val != null) {
                ret.put(item, "dbl : " + val);
            }
        });
        return ret;
    }


    public void toMapUseFunc() {

        List<Dish> dishs = new ArrayList<>();

        // 写2次的function
        Function<String, Long> parseLong = str -> Long.parseLong(str);
        Map<Long, Dish> dishMap1 = dishs.stream().collect(
                Collectors.toMap(item -> parseLong.apply(item.getId()), Function.identity(),
                        (oldValue, newValue) -> newValue));

        // 写一次的function
        Function<Dish, Long> keyMapper = dish -> {
            try {
                return Long.parseLong(dish.getId());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return -1L;
        };
        Map<Long, Dish> dishMap2 = dishs.stream().collect(
                Collectors.toMap(keyMapper, Function.identity(), (oldValue, newValue) -> newValue));

        System.out.println(dishMap1 == dishMap2);
    }


}

class Dish {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
