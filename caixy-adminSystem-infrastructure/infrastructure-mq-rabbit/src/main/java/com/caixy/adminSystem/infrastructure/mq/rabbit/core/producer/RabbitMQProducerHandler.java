package com.caixy.adminSystem.infrastructure.mq.rabbit.core.producer;

/**
 * 通用消息队列生产者接口
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.mq.producer.core.RabbitMQProducerHandler
 * @since 2024-06-20 14:52
 **/
public interface RabbitMQProducerHandler<T>
{
    /**
     * 发送消息的方法
     *
     * @param message   要发送的消息内容
     */
    void sendMessage(T message);

    void sendDelayMessage(T message);
}
