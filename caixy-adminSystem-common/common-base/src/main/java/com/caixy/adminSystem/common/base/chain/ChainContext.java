package com.caixy.adminSystem.common.base.chain;

import lombok.Getter;
import lombok.Setter;

/**
 * 责任链上下文数据
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/27 1:12
 */
@Getter
public class ChainContext<ContextType, ReturnType>
{
    private final ContextType data;

    private boolean interrupted = false;

    @Setter
    private String message;

    @Setter
    private ReturnType result;

    public ChainContext(ContextType data)
    {
        this.data = data;
    }

    public void interrupt()
    {
        this.interrupted = true;
    }
}
