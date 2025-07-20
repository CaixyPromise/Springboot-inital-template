package com.caixy.adminSystem.common.api.file.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 绑定文件访问权限
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/22 23:28
 */
@Getter
@RequiredArgsConstructor
public enum FileAccessLevelEnum
{
    PUBLIC(1, "公开"),
    PRIVATE(2, "私有"),
    PROTECT(3, "受保护"), // 受保护的文件，只有登录用户才能访问
    GRAND(4, "授权使用")  //  绑定者+授权token状态下才能访问
    ;
    private final Integer code;
    private final String label;

    // 根据code获取FileAccessLevelEnum
    public FileAccessLevelEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FileAccessLevelEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
