package com.caixy.adminSystem.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * Spring 上下文获取工具
 *
 
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    /**
     * 通过名称获取 Bean
     *
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    /**
     * 通过名称和类型获取 Bean
     *
     * @param beanName
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }

    /**
     * 初始化带有特定注解的服务，并返回注解值和服务的映射。
     *
     * @param serviceTypeList     服务实例列表
     * @param annotationTypeClass 注解类型的类对象
     * @param <ServiceType>       服务类型
     * @param <AnnotationType>    注解类型
     * @param <KeyType>           注解值的类型
     * @return 注解值和服务的映射
     */
    public static <ServiceType, AnnotationType extends Annotation, KeyType> HashMap<KeyType, ServiceType> getServiceFromAnnotation(
            List<ServiceType> serviceTypeList, Class<AnnotationType> annotationTypeClass)
    {
        HashMap<KeyType, ServiceType> serviceMap = new HashMap<>();

        for (ServiceType service : serviceTypeList)
        {
            // 获取实际类
            Class<?> targetClass = AopUtils.isAopProxy(service)
                                   ? AopProxyUtils.ultimateTargetClass(service)
                                   : service.getClass();

            // 获取实际类上的注解
            AnnotationType annotation = targetClass.getAnnotation(annotationTypeClass);
            if (annotation != null)
            {
                try
                {
                    // 获取注解中的 value 方法
                    Method valueMethod = annotationTypeClass.getMethod("value");
                    @SuppressWarnings("unchecked") KeyType key = (KeyType) valueMethod.invoke(annotation);
                    serviceMap.put(key, service);
                    System.out.printf("成功-初始化策略注解处理类：%s -> %s%n", key, targetClass.getName());
                }
                catch (ReflectiveOperationException e)
                {
                    throw new RuntimeException("Failed to process annotation value", e);
                }
            }
        }
        return serviceMap;
    }
}