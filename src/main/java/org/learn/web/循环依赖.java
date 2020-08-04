package org.learn.web;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.SimpleAliasRegistry;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @create: 2020-07-24 17:17
 * <p>
 * https://blog.csdn.net/f641385712/article/details/92801300
 */
public class 循环依赖 {


    /**
     * Spring的循环依赖的理论依据基于Java的引用传递，当获得对象的引用时，对象的属性是可以延后设置的，但是构造器必须是在获取引用之前。
     *
     * 对Bean的创建最为核心三个方法解释如下
     *
     *  1. createBeanInstance：例化，其实也就是调用对象的构造方法实例化对象
     *  2. populateBean：填充属性，这一步主要是对bean的依赖属性进行注入(@Autowired)
     *  3. initializeBean：回到一些形如initMethod、InitializingBean等方法
     *
     *      从对单例Bean的初始化可以看出，循环依赖主要发生在第二步（populateBean），也就是field属性注入的处理。
     *
     * */


    /**
     * 总结：：！
     * 1.使用context.getBean(A.class)，旨在获取容器内的单例A(若A不存在，就会走A这个Bean的创建流程)，显然初次获取A是不存在的，因此走A的创建之路~
     * 2.实例化A（注意此处仅仅是实例化），并将它放进缓存（此时A已经实例化完成，已经可以被引用了）
     * 3.初始化A：@Autowired依赖注入B（此时需要去容器内获取B）
     * 4.为了完成依赖注入B，会通过getBean(B)去容器内找B。但此时B在容器内不存在，就走向B的创建之路~
     * <p>
     * 5.实例化B，并将其放入缓存。（此时B也能够被引用了）
     * 6.初始化B，@Autowired依赖注入A（此时需要去容器内获取A）
     * 7.此处重要：初始化B时会调用getBean(A)去容器内找到A，上面我们已经说过了此时候因为A已经实例化完成了并且放进了缓存里，所以这个时候去看缓存里是已经存在A的引用了的，所以getBean(A)能够正常返回
     * 8.B初始化成功（此时已经注入A成功了，已成功持有A的引用了），return（注意此处return相当于是返回最上面的getBean(B)这句代码，回到了初始化A的流程中~）。
     * 9.因为B实例已经成功返回了，因此最终A也初始化成功
     * 10.到此，B持有的已经是初始化完成的A，A持有的也是初始化完成的B，完美~
     */


    abstract class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {
        // 从上至下 分表代表这“三级缓存”

        //1.用于存放完全初始化好的 bean，从该缓存中取出的 bean 可以直接使用
        private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256); //一级缓存

        //2.提前曝光的单例对象的cache，存放原始的 bean 对象（尚未填充属性），用于解决循环依赖
        private final Map<String, Object> earlySingletonObjects = new HashMap<>(16); // 二级缓存

        //3.单例对象工厂的cache，存放 bean 工厂对象，用于解决循环依赖
        private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16); // 三级缓存

        /**
         * Names of beans that are currently in creation.
         */
        // 这个缓存也十分重要：它表示bean创建过程中都会在里面呆着~
        // 它在Bean开始创建时放值，创建完成时会将其移出~
        private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

        /**
         * Names of beans that have already been created at least once.
         */
        // 当这个Bean被创建完成后，会标记为这个 注意：这里是set集合 不会重复
        // 至少被创建了一次的  都会放进这里~~~~
        private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<>(256));


        /**
         * 先从一级缓存singletonObjects中去获取。（如果获取到就直接return）
         * 如果获取不到或者对象正在创建中（isSingletonCurrentlyInCreation()），
         *      那就再从二级缓存earlySingletonObjects中获取。（如果获取到就直接return）
         *
         * 如果还是获取不到，且允许singletonFactories（allowEarlyReference=true）通过getObject()获取。
         * 就从三级缓存singletonFactory.getObject()获取。（
         *  如果获取到了就从singletonFactories中移除，并且放进earlySingletonObjects。其实也就是从三级缓存移动（是剪切、不是复制哦~）到了二级缓存）
         *
         * 加入singletonFactories三级缓存的前提是执行了构造器，所以构造器的循环依赖没法解决
         *
         * =================================================================================
         * 此处说一下二级缓存earlySingletonObjects它里面的数据什么时候添加什么移除？？?
         * 添加：向里面添加数据只有一个地方，就是上面说的getSingleton()里从三级缓存里挪过来
         * 移除：addSingleton、addSingletonFactory、removeSingleton从语义中可以看出
         *      添加单例、添加单例工厂ObjectFactory的时候都会删除二级缓存里面对应的缓存值，是互斥的
         */

        //获取单例Bean的源码如下：
        @Override
        @Nullable
        public Object getSingleton(String beanName) {
            return getSingleton(beanName, true);
        }

        @Nullable
        protected Object getSingleton(String beanName, boolean allowEarlyReference) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
                synchronized (this.singletonObjects) {
                    singletonObject = this.earlySingletonObjects.get(beanName);
                    if (singletonObject == null && allowEarlyReference) {
                        ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                        if (singletonFactory != null) {
                            singletonObject = singletonFactory.getObject();
                            this.earlySingletonObjects.put(beanName, singletonObject);
                            this.singletonFactories.remove(beanName);
                        }
                    }
                }
            }
            return singletonObject;
        }

        public boolean isSingletonCurrentlyInCreation(String beanName) {
            return this.singletonsCurrentlyInCreation.contains(beanName);
        }

        protected boolean isActuallyInCreation(String beanName) {
            return isSingletonCurrentlyInCreation(beanName);
        }
    }
}
