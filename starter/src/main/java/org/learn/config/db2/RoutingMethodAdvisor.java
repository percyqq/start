package org.learn.config.db2;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

/**
 * 路由方法Advisor
 *
 * @version V1.0
 * @date 2018/7/11 下午11:37
 */
public class RoutingMethodAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    @Override
    public Pointcut getPointcut() {
        return AnnotationMatchingPointcut.forMethodAnnotation(DataSourceRoutable.class);
    }
}
