package com.caixy.adminSystem.model.dto.oauth.github;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Github回调返回载体
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.oauth.github.GithubCallbackResponse
 * @since 2024/8/3 上午1:11
 */
@Data
@Builder
public class GithubCallbackResponse implements Serializable
{
    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 重定向地址
     */
    private String redirectUri;

    private static final long serialVersionUID = 1L;
}
