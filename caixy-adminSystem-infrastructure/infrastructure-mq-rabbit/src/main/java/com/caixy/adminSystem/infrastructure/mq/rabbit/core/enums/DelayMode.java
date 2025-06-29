package com.caixy.adminSystem.infrastructure.mq.rabbit.core.enums;

/**
 * 延迟模式注解
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/22 4:22
 */
public enum DelayMode
{
    /**
     * 普通队列 —— 不具备任何延迟功能。
     * 监听端直接消费，生产者发送时也不会带 x‑delay / TTL 等参数。
     */
    NONE,

    /**
     * 插件延迟队列 —— 使用 rabbitmq_delayed_message_exchange。
     * 声明一个类型为 "x-delayed-message" 的交换机；消息头里携带 x-delay 毫秒数。
     */
    PLUGIN,

    /**
     * TTL + DLX 延迟队列 —— 不依赖插件。
     * 队列本身设置 x-message-ttl；消息过期后通过 DLX 投递到真正业务队列。
     */
    TTL
}
