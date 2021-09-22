package org.learn.java;

import java.lang.annotation.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@interface XXX {
    String value() default "read";
}

public class 反射和切面 {
    int a;

    @XXX
    private void Ax() {
    }

    private void Bx() {
    }

    public static void main(String[] args) {
        String clazzName = "org.learn.java.反射和切面";
        Object proxy = new 反射和切面().createT(clazzName, null);
        //System.out.println(xxx.toString());

        反射和切面 xxx = (反射和切面) proxy;
        xxx.Ax();
    }


    public <T> T createT(String clazzName, Class<T> clazz) {

        try {
            final T t = (T) Class.forName(clazzName).newInstance();

            T real = (T) Proxy.newProxyInstance(getClass().getClassLoader(), t.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String methodName = method.getName();

                    Method method1 = t.getClass().getMethod(methodName, method.getParameterTypes());
                    XXX annotation = method1.getAnnotation(XXX.class);

                    if (annotation == null) {
                        return method.invoke(t, args);
                    }

                    String val = annotation.value();
                    if ("read".equals(val)) {

                    }

                    return null;
                }
            });
            return real;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}


