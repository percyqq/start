package org.learn.java.继承.新包;

import org.learn.java.继承.ABC;

// https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247484210&idx=1&sn=9d40e2e4c72f0727c7b7925cbe314fc0&chksm=ebd74233dca0cb2560677c7dc7746bf166195d793860c41ab477431af2cf0a6004477e27b814&scene=21###wechat_redirect
public class B extends ABC {
    int getB() {
        return getA();
    }

    public static void main(String[] args) {
        new B().getA();

        //  protected修饰的类和属性,对于自己、本包和其子类可见，这句话本身是没有错的。
        //      但是还需要补充：对于protected的成员或方法，要分子类和超类是否在同一个包中。
        //          与基类不在同一个包中的子类，只能访问自身从基类继承而来的受保护成员，而不能访问基类实例本身的受保护成员。
        // compile error   new C().getA();
    }
}

class C extends ABC {
    int getB() {
        return getA();
    }

    public static void main(String[] args) {
        // compile error   new B().getA();
        new C().getA();
    }
}