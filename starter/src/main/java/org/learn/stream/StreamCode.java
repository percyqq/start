package org.learn.stream;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    public static void main1(String[] args) {
        List<String> data = Lists.newArrayList("1", "3", "5", "7", "9");
        Spliterator<List<String>> spliterator = Iterables.partition(data, 2).spliterator();
        Map<String, String> kkk = StreamSupport.stream(spliterator, false).map(StreamCode::genData)
                .reduce(new HashMap<>(), (retMap, loopDataMap) -> {
                    retMap.putAll(loopDataMap);
                    return retMap;
                });
        System.out.println(kkk);
    }

    public static void main(String[] args) {
        List<Integer> d = Lists.newArrayList(1, 2, 3, 4, 5).stream().filter(e -> e % 2 == 0).collect(Collectors.toList());
        System.out.println(d);
    }

    public static void multiSort() {
        List<BigVo> list = new ArrayList<>();
        Function<BigVo, Date> function = bigVo -> {
            Optional<Date> showTime = Optional.ofNullable(bigVo.getCityConfig()).map(CityConfig::getStartTime);
            Optional<Date> searchTime = Optional.ofNullable(bigVo.getHouseSearchRecord()).map(HouseSearchRecord::getCreateTs);
            return Stream.of(showTime, searchTime)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
        };
        Comparator<BigVo> comparator = Comparator.comparing(function).reversed();

        list.stream().sorted(comparator).collect(Collectors.toList());
    }

    private void x(){
        List<Integer> data = new ArrayList<>();
        List<Integer> businessVoList = parallelList(new ForkJoinPool(4), data, integer -> integer);

    }


    public static <In, Out> List<Out> parallelList(ForkJoinPool customThreadPool, List<In> data, Function<In, Out> function) {
        List<Out> ret;
        try {
            ret = customThreadPool.submit(() -> data.parallelStream().map(function).collect(Collectors.toList())).get();
        } catch (InterruptedException e) {
            ret = Collections.emptyList();
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            ret = Collections.emptyList();
            e.printStackTrace();
        }

        return ret;
    }

    public static <T, K, U> Map<K, U> parallelMap(ForkJoinPool customThreadPool, List<T> data, Collector<T, ?, Map<K, U>> collector) {
        Map<K, U> ret;
        try {
            ret = customThreadPool.submit(() -> data.parallelStream().collect(collector)).get();
        } catch (InterruptedException e) {
            ret = Collections.emptyMap();
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            ret = Collections.emptyMap();
            e.printStackTrace();
        }

        return ret;
    }


    public static void compare() throws ParseException {
        List<CityConfig> dd = new ArrayList<>();
        CityConfig s1 = new CityConfig();
        s1.setHouseCode(1);
        s1.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-11-09 15:44:33"));

        CityConfig s2 = new CityConfig();
        s2.setHouseCode(2);
        s2.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-11-09 15:41:33"));

        CityConfig s3 = new CityConfig();
        s3.setHouseCode(1);
        s3.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-11-09 15:45:33"));

        CityConfig s4 = new CityConfig();
        s4.setHouseCode(1);
        s4.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-11-09 15:55:33"));
        dd.add(s1);
        dd.add(s2);
        dd.add(s3);
        dd.add(s4);

        Map<String, CityConfig> ss = dd.stream().collect(
                Collectors.toMap(i -> i.getHouseCode().toString(), Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(CityConfig::getStartTime))));
        ss.forEach((k, v) -> System.out.println(v.getHouseCode() + " ==> " + v.getStartTime()));
    }


    private void 按照属性进行过滤() {
        List<按照属性进行过滤User> wtf = new ArrayList<>();
        List<按照属性进行过滤User> data = wtf.stream().filter(按照属性进行过滤_distinctByKey(user -> user.getUserCode())).map(recipient -> {
            按照属性进行过滤User user = new 按照属性进行过滤User();
            user.setUserCode(recipient.getUserCode());
            user.setName(recipient.getName());
            return user;
        }).collect(Collectors.toList());
    }

    <T> Predicate<T> 按照属性进行过滤_distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Data
    public class 按照属性进行过滤User {
        private String userCode;
        private String name;
    }

    private CityConfig getNextToAssign(List<CityConfig> configs) {
        Comparator wtf = Comparator.comparing(
                CityConfig::getLatestAssignTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(CityConfig::getOrder);
        Optional<CityConfig> optionalCityConfig = configs.stream().min(wtf);
        return optionalCityConfig.orElseThrow(IllegalStateException::new);
    }

    @Data
    public static class BigVo {
        private CityConfig cityConfig;
        private HouseSearchRecord houseSearchRecord;
    }

    @Data
    public static class HouseSearchRecord {
        private Date createTs;
    }

    @Data
    public static class CityConfig {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date latestAssignTime;

        private Integer order;
        private Date startTime;
        private Integer houseCode;
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
                Collectors.toMap(item -> parseLong.apply(item.getId()), Function.identity(), (oldValue, newValue) -> newValue));

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
