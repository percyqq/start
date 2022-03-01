package org.learn.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

interface Wtf {
    int stg();
}

class WtfImpl implements Wtf {
    @Override
    public int stg() {
        return 135;
    }
}

class WtfHandler implements InvocationHandler {
    Wtf wtf;

    public WtfHandler(Wtf wtf) {
        this.wtf = wtf;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("wtf start");
        Object obj = method.invoke(wtf, args);
        System.out.println("wtf end ==> " + obj);
        return obj;
    }

}

public class jdk动态代理 {
    public static void main(String[] args) {
        Wtf wtf = new WtfImpl();
        InvocationHandler handler = new WtfHandler(wtf);
        Wtf proxy = (Wtf) Proxy.newProxyInstance(
                wtf.getClass().getClassLoader(), wtf.getClass().getInterfaces(), handler);

        System.out.println(111);
        System.out.println(proxy.stg());
    }



























}
