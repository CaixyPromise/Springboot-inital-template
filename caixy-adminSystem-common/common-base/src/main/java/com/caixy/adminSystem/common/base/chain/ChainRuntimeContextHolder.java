package com.caixy.adminSystem.common.base.chain;

/**
 * ThreadLocal 持有当前执行链路的运行时上下文
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/25 17:00
 */
public class ChainRuntimeContextHolder {
    private static final ThreadLocal<ChainRuntimeContext> HOLDER =
            ThreadLocal.withInitial(ChainRuntimeContext::new);

    public static ChainRuntimeContext current() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}