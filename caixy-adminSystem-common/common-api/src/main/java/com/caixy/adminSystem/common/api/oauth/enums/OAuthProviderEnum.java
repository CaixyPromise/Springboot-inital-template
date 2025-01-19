package com.caixy.adminSystem.common.api.oauth.enums;

import com.caixy.adminSystem.common.base.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth验证服务端类型枚举
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.onlineJudge.models.enums.oauth2.OAuthProviderEnum
 * @since 2024/8/6 上午2:04
 */
@Getter
@AllArgsConstructor
public enum OAuthProviderEnum
{
    GITHUB("GitHub", "github"),

    ;

    private final String providerName;
    private final String providerCode;

    public static OAuthProviderEnum getProviderEnum(String providerCode)
    {
        if (StringUtils.isAnyBlank(providerCode))
        {
            return null;
        }
        for (OAuthProviderEnum providerEnum : values())
        {
            if (providerEnum.getProviderCode().equals(providerCode))
            {
                return providerEnum;
            }
        }
        return null;
    }
}
