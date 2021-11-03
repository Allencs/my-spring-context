package com.allen.application;

import com.allen.spring.BeanPostProcessor;
import com.allen.spring.annotations.Component;

/**
 * 可实现AOP功能：postProcessBeforeInitialization ｜ postProcessAfterInitialization返回代理对象
 * 使用CGLIB生成代理对象
 */

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            System.out.println(bean.getClass().toString() + " | 执行postProcessBeforeInitialization。");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (beanName.equals("orderService")) {
            System.out.println(bean.getClass().toString() + " | 执行postProcessAfterInitialization。");
            // 创建代理类对象proxyInstance
        }
        return bean;
    }
}
