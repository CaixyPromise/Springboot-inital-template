package com.caixy.adminSystem.model.dto.oauth.github;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * github的oauth获取验证地址请求体
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.oauth.github.GithubGetAuthorizationUrlRequest
 * @since 2024/8/3 上午1:06
 */
@Data
@AllArgsConstructor
public class GithubGetAuthorizationUrlRequest implements Serializable
{
    /**
     * 成功之后返回的地址（前端地址）
     */
    private String redirectUri;

    private String sessionId;

    private static final long serialVersionUID = 1L;
}
