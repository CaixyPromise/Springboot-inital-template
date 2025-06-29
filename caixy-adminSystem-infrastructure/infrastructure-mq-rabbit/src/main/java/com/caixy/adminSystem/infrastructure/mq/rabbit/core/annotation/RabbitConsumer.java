package com.caixy.adminSystem.infrastructure.mq.rabbit.core.annotation;


import com.caixy.adminSystem.infrastructure.mq.rabbit.core.consumer.RabbitConsumerRegistrar;
import com.caixy.adminSystem.infrastructure.mq.rabbit.core.enums.RabbitMQQueueEnum;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 自定义消息监听器
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/13 1:35
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Import(RabbitConsumerRegistrar.class)     // 把 Registrar 注入 Spring
public @interface RabbitConsumer {

    RabbitMQQueueEnum value();             // 必填：枚举

    /** 可选：并发数、是否自动启动 */
    String  concurrency() default "1";
    boolean autoStartup() default true;
}

