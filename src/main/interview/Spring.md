https://blog.csdn.net/f641385712/article/details/92801300
三级缓存“巧妙解决Bean的循环依赖

对Bean的创建最为核心三个方法解释如下：
    1. createBeanInstance：例化，其实也就是调用对象的构造方法实例化对象
    2. populateBean：填充属性，这一步主要是对bean的依赖属性进行注入(@Autowired)
    3. initializeBean：回到一些形如initMethod、InitializingBean等方法
    从对单例Bean的初始化可以看出，循环依赖主要发生在第二步（populateBean），也就是field属性注入的处理。

// 从上至下 分表代表这“三级缓存”
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256); //一级缓存
	private final Map<String, Object> earlySingletonObjects = new HashMap<>(16); // 二级缓存
	private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16); // 三级缓存
	...


1.先从一级缓存singletonObjects中去获取。（如果获取到就直接return）
2.如果获取不到或者对象正在创建中（isSingletonCurrentlyInCreation()），那就再从二级缓存earlySingletonObjects中获取。
    （如果获取到就直接return）
3.如果还是获取不到，且允许singletonFactories（allowEarlyReference=true）通过getObject()获取。
    就从三级缓存singletonFactory.getObject()获取。
    （如果获取到了就从singletonFactories中移除，并且放进earlySingletonObjects。
    其实也就是从三级缓存移动（是剪切、不是复制哦~）到了二级缓存）
#加入singletonFactories三级缓存的前提是执行了构造器，所以构造器的循环依赖没法解决

[getSingleton()从缓存里获取单例对象步骤分析可知，Spring解决循环依赖的诀窍：就在于singletonFactories这个三级缓存。
    这个Cache里面都是ObjectFactory，它是解决问题的关键]


最后的最后，由于我太暖心了_，再来个纯文字版的总结。
依旧以上面A、B类使用属性field注入循环依赖的例子为例，对整个流程做文字步骤总结如下：

1. 使用context.getBean(A.class)，旨在获取容器内的单例A(若A不存在，就会走A这个Bean的创建流程)，
    显然初次获取A是不存在的，因此走A的创建之路~
2. 实例化A（注意此处仅仅是实例化），并将它放进缓存（此时A已经实例化完成，已经可以被引用了）
3. 初始化A：@Autowired依赖注入B（此时需要去容器内获取B）
4. 为了完成依赖注入B，会通过getBean(B)去容器内找B。但此时B在容器内不存在，就走向B的创建之路~
5. 实例化B，并将其放入缓存。（此时B也能够被引用了）
6. 初始化B，@Autowired依赖注入A（此时需要去容器内获取A）
7. 此处重要：初始化B时会调用getBean(A)去容器内找到A，上面我们已经说过了此时候因为A已经实例化完成了并且放进了缓存里，
    所以这个时候去看缓存里是已经存在A的引用了的，所以getBean(A)能够正常返回
8. B初始化成功（此时已经注入A成功了，已成功持有A的引用了），
    return（注意此处return相当于是返回最上面的getBean(B)这句代码，回到了初始化A的流程中~）。
9. 因为B实例已经成功返回了，因此最终A也初始化成功
10. 到此，B持有的已经是初始化完成的A，A持有的也是初始化完成的B，完美~