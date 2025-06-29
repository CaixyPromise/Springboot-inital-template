package com.caixy.adminSystem.common.base.aop;

import com.caixy.adminSystem.common.base.annotation.StaticInject;
import com.caixy.adminSystem.common.base.annotation.StaticValue;
import com.caixy.adminSystem.common.base.annotation.StaticValueTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 注入spring boot配置到静态属性方法
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/25 4:28
 */

@Component
@RequiredArgsConstructor
public class StaticValueInjectorAspect
{

    private final ApplicationContext context;
    private final Environment env;
    private final ConversionService conversionService;



    @PostConstruct
    public void injectStaticFields() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(StaticValueTarget.class));

        // set by MainApplication
        String basePackage = System.getProperty("mainClass.basePackage");

        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());

            // 1. 处理字段 @StaticValue 注入
            ReflectionUtils.doWithFields(clazz, field -> {
                StaticValue staticValue = field.getAnnotation(StaticValue.class);
                if (staticValue != null) {
                    injectField(field, staticValue);
                }
            }, field -> Modifier.isStatic(field.getModifiers()));

            // 2️. 处理方法 @StaticInject 注入
            ReflectionUtils.doWithMethods(clazz, method -> {
                StaticInject inject = method.getAnnotation(StaticInject.class);
                if (inject != null && Modifier.isStatic(method.getModifiers())) {
                    injectStaticMethod(method, inject);
                }
            });
        }
    }

    private void injectField(Field field, StaticValue ann) {
        String key = ann.value();
        String raw = env.getProperty(key, ann.defaultValue());

        if (raw.isEmpty()) {
            throw new IllegalStateException("配置键不存在且无默认值: " + key);
        }

        Object value = conversionService.convert(raw, field.getType());
        field.setAccessible(true);
        ReflectionUtils.setField(field, null, value);

        if (!ann.onSucceed().isEmpty()) {
            try {
                Method method;
                if (ann.parameterized()) {
                    // 有参数版本
                    method = field.getDeclaringClass().getDeclaredMethod(ann.onSucceed(), field.getType());
                    if (!Modifier.isStatic(method.getModifiers())) {
                        throw new IllegalStateException("onSucceed 必须是静态方法: " + ann.onSucceed());
                    }
                    method.setAccessible(true);
                    method.invoke(null, value);
                } else {
                    // 无参数版本
                    method = field.getDeclaringClass().getDeclaredMethod(ann.onSucceed());
                    if (!Modifier.isStatic(method.getModifiers())) {
                        throw new IllegalStateException("onSucceed 必须是静态方法: " + ann.onSucceed());
                    }
                    method.setAccessible(true);
                    method.invoke(null);
                }
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("找不到 onSucceed: " + ann.onSucceed(), e);
            } catch (Exception e) {
                throw new RuntimeException("执行 onSucceed 失败: " + ann.onSucceed(), e);
            }
        }
    }

    private void injectStaticMethod(Method method, StaticInject inject) {
        try {
            Object dependency = context.getBean(inject.value());
            method.setAccessible(true);
            method.invoke(null, dependency); // 调用静态方法
        } catch (Exception e) {
            throw new RuntimeException("调用注入方法失败: " + method.getDeclaringClass().getName() + "." + method.getName(), e);
        }
    }
}
