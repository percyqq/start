深入解析String#intern
https://tech.meituan.com/2014/03/06/in-depth-understanding-string-intern.html

JAVA 使用 jni 调用c++实现的StringTable的intern方法, StringTable的intern方法跟Java中的HashMap的实现是差不多的, 只是不能自动扩容。默认大小是1009。
要注意的是，String的String Pool是一个固定大小的Hashtable，默认值大小长度是1009，如果放进String Pool的String非常多，就会造成Hash冲突严重，
从而导致链表会很长，而链表长了后直接会造成的影响就是当调用String.intern时性能会大幅下降（因为要一个一个找）。

在 jdk6中StringTable是固定的，就是1009的长度，所以如果常量池中的字符串过多就会导致效率下降很快。在jdk7中，StringTable的长度可以通过一个参数指定：
-XX:StringTableSize=99991
相信很多 JAVA 程序员都做做类似 String s = new String("abc")这个语句创建了几个对象的题目。 
这种题目主要就是为了考察程序员对字符串对象的常量池掌握与否。
上述的语句中是创建了2个对象，第一个对象是”abc”字符串存储在常量池中，第二个对象在JAVA Heap中的 String 对象。











