package com.hzau.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.common.utils
 * @className: SpringBeanFactory
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/22 下午8:46
 */
@Component
public class SpringBeanFactory implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanFactory.applicationContext = applicationContext;
    }

    /**
     * 根据Bean名称获取Bean
     * @param beanName Bean名称
     * @return Bean实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return (T) applicationContext.getBean(beanName);
        } catch (BeansException e) {
            return null;
        }
    }

    /**
     * 根据Bean类型获取Bean
     * @param clazz Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            return null;
        }
    }

    /**
     * 根据Bean名称和类型获取Bean
     * @param beanName Bean名称
     * @param clazz Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(beanName, clazz);
        } catch (BeansException e) {
            return null;
        }
    }
}
