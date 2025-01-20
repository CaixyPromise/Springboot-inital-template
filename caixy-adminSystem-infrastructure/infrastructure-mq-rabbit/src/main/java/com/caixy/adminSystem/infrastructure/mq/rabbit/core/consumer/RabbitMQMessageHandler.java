package com.caixy.adminSystem.infrastructure.mq.rabbit.core.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

/**
 * 消息处理接口
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.mq.consumer.core.RabbitMQMessageHandler
 * @since 2024-06-20 13:15
 **/
public interface RabbitMQMessageHandler<T>
{

    /**
     * 处理接收到的消息
     *
     * @param messageDto 反序列化后的消息对象
     * @param channel    RabbitMQ的Channel对象
     * @param message    原始消息对象
     * @param messageId  消息ID
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    void handleMessage(T messageDto, Channel channel, Message message, String messageId) throws Exception;

    /**
     * 处理死信队列中的消息
     *
     * @param messageDto 反序列化后的消息对象
     * @param channel    RabbitMQ的Channel对象
     * @param message    消息对象
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    void handleDeadLetterMessage(T messageDto, Channel channel, Message message, String messageId) throws Exception;
}