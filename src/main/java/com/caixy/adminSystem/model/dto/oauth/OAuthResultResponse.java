package com.caixy.adminSystem.model.dto.oauth;

import com.caixy.adminSystem.model.dto.user.UserLoginByOAuthAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * OAuth验证最终返回结果
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.oauth.OAuthResultResponse
 * @since 2024/8/7 上午12:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuthResultResponse implements Serializable
{
    /**
     * 返回结果
     */
    private Object result;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 重定向地址
     */
    private String redirectUrl;

    /**
     * 登录适配器
     */
    private UserLoginByOAuthAdapter loginAdapter;

    public Boolean isSuccess(){
        return success;
    }
    private static final long serialVersionUID = 1L;
}
