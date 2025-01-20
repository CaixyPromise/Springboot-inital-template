package com.caixy.adminSystem.infrastructure.mq.rabbit.core.producer;


import com.caixy.adminSystem.infrastructure.mq.rabbit.core.annotation.RabbitProducer;
import com.caixy.adminSystem.infrastructure.mq.rabbit.core.enums.RabbitMQQueueEnum;
import com.caixy.adminSystem.infrastructure.mq.rabbit.core.manager.RabbitMQManager;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 通用生产者方法类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.mq.producer.core.GenericRabbitMQProducer
 * @since 2024-06-20 15:15
 **/
@Slf4j
public abstract class GenericRabbitMQProducer<T> implements RabbitMQProducerHandler<T>
{
    @Resource
    private RabbitMQManager rabbitMQUtils;

    protected final RabbitMQQueueEnum queueEnum;

    protected GenericRabbitMQProducer()
    {
        if (this.getClass().isAnnotationPresent(RabbitProducer.class))
        {
            RabbitProducer rabbitProducer = this.getClass().getAnnotation(RabbitProducer.class);
            queueEnum = rabbitProducer.value();
        }
        else
        {
            throw new RuntimeException("Rabbit生产者初始化失败：RabbitProducer注解未找到");
        }
    }

    // 默认的发送消息方法可以在这里实现，子类可以覆盖这个方法
    @Override
    public void sendMessage(T message)
    {
        if (!queueEnum.getIsDelay())
        {
            rabbitMQUtils.sendMessage(queueEnum, message);
            log.info("发送消息成功，队列：{}，消息：{}", queueEnum.getQueueName(), message);
        }
        else
        {
            sendDelayMessage(message);
            log.warn("{}: 使用发送普通消息方法{}，消息：{}", queueEnum.getQueueName(), queueEnum.getQueueName(), message);
        }
    }


    @Override
    public void sendDelayMessage(T message)
    {
        if (!queueEnum.getIsDelay()) {
            log.warn("队列: {} 不是延时队列，不能发送延时消息; 已发送普通消息", queueEnum.getQueueName());
            rabbitMQUtils.sendMessage(queueEnum, message);
            return;
        }
        rabbitMQUtils.sendDelayedMessage(queueEnum, message);

        log.info("发送延时消息成功，队列：" + queueEnum.getQueueName() + "，消息：" + message);
    }
}
