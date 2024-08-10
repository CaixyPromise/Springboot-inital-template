package com.caixy.adminSystem.model.enums;

import com.caixy.adminSystem.model.dto.oauth.github.GithubUserProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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
    GITHUB("GitHub", "github", GithubUserProfileDTO.class),

    ;

    private final String providerName;
    private final String providerCode;
    private final Class<?> UserProfileType;

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
