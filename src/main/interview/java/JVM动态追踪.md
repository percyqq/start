Java动态追踪技术探究
https://tech.meituan.com/2019/02/28/java-dynamic-trace.html

java.lang.instrument.Instrumentation  
    看完文档之后，我们发现这么两个接口：redefineClasses和retransformClasses。  
    一个是重新定义class，一个是修改class。这两个大同小异，看reDefineClasses的说明：  
      This method is used to replace the definition of a class without reference to the existing class file bytes,   
      as one might do when recompiling from source for fix-and-continue debugging.   
      Where the existing class file bytes are to be transformed (for example in bytecode instrumentation) retransformClasses should be used.


从Java 8开始，JDK使用invokedynamic及VM Anonymous Class结合来实现Java语言层面上的Lambda表达式。

invokedynamic： invokedynamic是Java 7为了实现在JVM上运行动态语言而引入的一条新的虚拟机指令，它可以实现在运行期动态解析出调用点限定符所引用的方法，然后再执行该方法，invokedynamic指令的分派逻辑是由用户设定的引导方法决定。
VM Anonymous Class：可以看做是一种模板机制，针对于程序动态生成很多结构相同、仅若干常量不同的类时，可以先创建包含常量占位符的模板类，
而后通过Unsafe.defineAnonymousClass方法定义具体类时填充模板的占位符生成具体的匿名类。生成的匿名类不显式挂在任何ClassLoader下面，只要当该类没有存在的实例对象、
且没有强引用来引用该类的Class对象时，该类就会被GC回收。故而VM Anonymous Class相比于Java语言层面的匿名内部类无需通过ClassClassLoader进行类加载且更易回收。

在Lambda表达式实现中，通过invokedynamic指令调用引导方法生成调用点，在此过程中，会通过ASM动态生成字节码，而后利用Unsafe的defineAnonymousClass方法定义实现相应的函数式接口的匿名类，
然后再实例化此匿名类，并返回与此匿名类中函数式方法的方法句柄关联的调用点；而后可以通过此调用点实现调用相应Lambda表达式定义逻辑的功能。


典型应用
常规对象实例化方式：我们通常所用到的创建对象的方式，从本质上来讲，都是通过new机制来实现对象的创建。但是，new机制有个特点就是当类只提供有参的构造函数且无显示声明无参构造函数时，
则必须使用有参构造函数进行对象构造，而使用有参构造函数时，必须传递相应个数的参数才能完成对象实例化。
非常规的实例化方式：而Unsafe中提供allocateInstance方法，仅通过Class对象就可以创建此类的实例对象，而且不需要调用其构造函数、初始化代码、JVM安全检查等。
它抑制修饰符检测，也就是即使构造器是private修饰的也能通过此方法实例化，只需提类对象即可创建相应的对象。
由于这种特性，allocateInstance在java.lang.invoke、Objenesis（提供绕过类构造器的对象生成方式）、Gson（反序列化时用到）中都有相应的应用。


ASM	  访问者模式。

JVMTI & Agent & Attach API
上一小节中，我们给出了Agent类的代码，追根溯源需要先介绍JPDA（Java Platform Debugger Architecture）


java.lang.instrument.Instrumentation
这么两个接口	redefineClasses和retransformClasses。一个是重新定义class，一个是修改class。
BTrace基于ASM、Java Attach Api、Instruments开发，为用户提供了很多注解。依靠这些注解，我们可以编写BTrace脚本（简单的Java代码）达到我们想要的效果，而不必深陷于ASM对字节码的操作中不可自拔。

BTrace最终借Instruments实现class的替
基于Java的Attach Api，Agent可以动态附着到一个运行的JVM上，然后开启一个BTrace Server，接收client发过来的BTrace脚本；解析脚本，然后根据脚本中的规则找到要修改的类；
修改字节码后，调用Java Instrument的reTransform接口，完成对对象行为的修改并使之生效。

如上文所说，出于安全考虑，Instruments在使用上存在诸多的限制，BTrace也不例外。BTrace对JVM来说是“只读的”，因此BTrace脚本的限制如下：
不允许创建对象
不允许创建数组
不允许抛异常
不允许catch异常
不允许随意调用其他对象或者类的方法，只允许调用com.sun.btrace.BTraceUtils中提供的静态方法（一些数据处理和信息输出工具）
不允许改变类的属性
不允许有成员变量和方法，只允许存在static public void方法
不允许有内部类、嵌套类
不允许有同步方法和同步块
不允许有循环
不允许随意继承其他类（当然，java.lang.Object除外）
不允许实现接口
不允许使用assert
不允许使用Class对象





