package com.caixy.adminSystem.common.base.chain;

/**
 * 处理器接口（责任链节点）
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/27 1:13
 */
public interface ChainHandler<ContextType, ReturnType>
{
    /**
     * 处理当前节点逻辑
     * @param context 责任链上下文
     */
    void handle(ChainContext<ContextType, ReturnType> context);

    /**
     * 默认方法：隐式获取运行时上下文，无需显式传参
     */
    default ChainRuntimeContext runtimeContext() {
        return ChainRuntimeContextHolder.current();
    }
}
