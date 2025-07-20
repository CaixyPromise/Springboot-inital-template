package com.caixy.adminSystem.infrastructure.file.domain.enums;

import com.caixy.adminSystem.common.base.constant.BaseCacheEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * 文件业务枚举
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/30 上午12:58
 */
@Getter
@AllArgsConstructor
public enum FileRedisBizKey implements BaseCacheEnum
{
    UPLOAD_EXIST_TOKEN("upload_exist:", 10L, TimeUnit.MINUTES),

    ;
    private final String key;
    private final Long expire;
    private final TimeUnit timeUnit;
}
