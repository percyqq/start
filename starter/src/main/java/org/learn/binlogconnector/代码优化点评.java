package org.learn.binlogconnector;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class 代码优化点评 {

    private static Map<String, String> 当前类的变量 = new HashMap<>();

    public static void main(String[] args) {
        // 使用function函数 优化 BinlogTaskImpl 中的 callback
        //  BinlogEventParseCallback eventParseCallback = new AbstractBinlogEventParseCallbackImpl(taskInfo, tableOperate, targetBinlogRepository,

        String inputToUse= "sth wtf";
        wtf(inputToUse, dataMadeByFunction -> {
            String todo = 当前类的变量.get(dataMadeByFunction);

            // 在另外一个类中使用了回调来使用当前类的变量。
            System.out.println(todo);
        });
    }


    private static void wtf(String in, Consumer<String> action) {
        System.out.println("step 1");

        String input = in.toLowerCase(Locale.ROOT);
        action.accept(input);

        System.out.println("step 3");
    }


}
