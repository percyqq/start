package org.learn.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @create: 2020-07-24 13:54
 */
public class StreamCode {


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
