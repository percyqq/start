package org.learn;

public class Kafka应用 {


    //一个表一个topic，一个分区， 保证消息顺序

    // 当需要全量同步单表数据，使用kafka admin client 删除 topic
    // 此时一定会触发Rebalance，所以，需要使用 auto-offset-reset= earliest
    // 如果使用latest，kafka admin client 新建了 topic。此时仍然在重平衡，会导致丢失这期间提交的数据，

    // 使用场景 ： 动态创建监听者，动态发布topic，因此使用latest需要面对的问题 ： 当创建监听者于 发布topic之后，
    //      会导致无法拉取到   topic建立[A] -- 发送消息[B] -- 监听者建立[C]   ==>  [B] - [C]的消息会丢。

    // 下面这个结论需要验证一下
    //   ## 提交过offset，latest和earliest没有区别，但是在没有提交offset情况下，用latest直接会导致无法读取旧数据 ##

    /**
     我们在前面说过，
     Rebalance 发生的时机有三个：
     1.组成员数量发生变化
     2.订阅主题数量发生变化
     3.订阅主题的分区数发生变化

     17丨消费者组重平衡能避免吗？.html
     **/

}
