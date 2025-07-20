package com.caixy.adminSystem.common.base.chain;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行器（责任链调度器）
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/27 1:13
 */
public class ChainExecutor<ContextType, ReturnType>
{
    private final List<ChainHandler<ContextType, ReturnType>> handlers = new ArrayList<>();

    /** 注册责任链节点 */
    public ChainExecutor<ContextType, ReturnType> addHandler(
            ChainHandler<ContextType, ReturnType> handler) {
        handlers.add(handler);
        return this;
    }

    /** 执行责任链 */
    public ReturnType execute(ContextType data) {
        // 1. 初始化运行时上下文
        ChainRuntimeContextHolder.clear();  // 清理旧上下文

        // 2. 构造业务上下文
        ChainContext<ContextType, ReturnType> context = new ChainContext<>(data);
        try {
            for (ChainHandler<ContextType, ReturnType> handler : handlers) {
                handler.handle(context);
                if (context.isInterrupted()) {
                    break;
                }
            }
            return context.getResult();
        } finally {
            // 3. 最终清理，防止内存泄漏
            ChainRuntimeContextHolder.clear();
        }
    }
}