package com.caixy.adminSystem.infrastructure.mq.rabbit.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;


/**
 * @name: com.caixy.adminSystem.model.enums.RabbitMQQueueEnum
 * @description: 普通消息队列信息枚举封装
 * @author: CAIXYPROMISE
 * @date: 2024-06-19 22:30
 **/
@AllArgsConstructor
@Getter
public enum RabbitMQQueueEnum
{
    /**
    * 链接统计保存队列
    */
    LINK_STATS("LinkStatsSaveExchange",
            "link.stats.save",
            "linkStatsSaveQueue",
            "X-DeadLetter-Link-Stats-Save-Queue"
            )
    ;
    /**
     * 交换机名称
     */
    private final String exchange;

    /**
     * 路由键
     */
    private final String routingKey;

    /**
     * 队列名称
     */
    private final String queueName;

    /**
     * 死信队列名称
     */
    private final String deadLetterQueue;

    /**
     * 延迟时间
     */
    private final Long delayTime;

    /**
     * 是否是延迟队列
     */
    private final Boolean isDelay;

    /**
    * 是否手动ack
    */
    private final Boolean manualAck;

    RabbitMQQueueEnum(String exchange, String routingKey, String queueName, String deadLetterQueue)
    {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.queueName = queueName;
        this.deadLetterQueue = deadLetterQueue;
        this.delayTime = null;
        this.isDelay = false;
        this.manualAck = true;
    }


    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static RabbitMQQueueEnum getEnumByValue(String value)
    {
        if (ObjectUtils.isEmpty(value))
        {
            return null;
        }
        for (RabbitMQQueueEnum anEnum : RabbitMQQueueEnum.values())
        {
            if (anEnum.routingKey.equals(value))
            {
                return anEnum;
            }
        }
        return null;
    }
}
