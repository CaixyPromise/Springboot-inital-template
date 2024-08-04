package com.caixy.adminSystem.model.dto.oauth.github;

import lombok.Data;

import java.io.Serializable;

/**
 * Github验证Callback地址
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.oauth.github.GithubCallbackRequest
 * @since 2024/8/3 上午1:09
 */
@Data
public class GithubCallbackRequest implements Serializable
{
    /**
     * github返回的code
     */
    private String code;

    /**
     * github返回的state
     */
    private String state;

    /**
     * session id
     */
    private String sessionId;
    
    /**
     * 错误信息
     */
    private String error;

    private static final long serialVersionUID = 1L;
}
