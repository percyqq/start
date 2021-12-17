package org.learn.binlogconnector;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class 代码优化点评 {

    private static Map<String, String> 当前类的变量 = new HashMap<>();


    //!
    // EventGroup       ==> BinlogEventParser
    // 将各类事件使用单独的 XX-Event类，调用对应的方法  onEvent（Event类）处理回调事件
    // EventGroup  是一组完整的事件，比如DDL, DML ， 完整的一个事务等。
//!




    public static void main(String[] args) {
        // 使用function函数 优化 BinlogTaskImpl 中的 callback
        //  BinlogEventParseCallback eventParseCallback = new AbstractBinlogEventParseCallbackImpl(taskInfo, tableOperate, targetBinlogRepository,

        String inputToUse= "sth wtf";
        wtf(inputToUse, dataMadeByFunction -> {
            String todo = 当前类的变量.get(dataMadeByFunction);

            // 在另外一个类中使用了回调来使用当前类的变量。
            System.out.println(todo);
        });

        System.out.println("-----");
        wtf2(inputToUse, dataMadeByFunction -> {
            String todo = dataMadeByFunction + "碉堡了";
            // 在另外一个类中使用了回调来使用当前类的变量。
            // 并且回传给当前函数。 相当于函数嵌套调用，但是 嵌套调用的函数内部使用了 外部函数的变量，而没有通过传递，
            System.out.println(todo);
            return todo;
        });
    }

    private static void wtf(String in, Consumer<String> action) {
        System.out.println("step 1");
        String input = in.toLowerCase(Locale.ROOT);
        action.accept(input);
        System.out.println("step 3");
    }

    private static void wtf2(String input, Function<String, String> action) {
        System.out.println("step 1");
        String ret2 = action.apply(input);
        System.out.println("step 3, Function ret add : (" + ret2 + ")");
    }



}
