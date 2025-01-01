package com.caixy.adminSystem.common.api.user.dto;

import com.github.houbb.sensitive.annotation.strategy.SensitiveStrategyPassword;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 */
@Data
public class UserLoginRequest implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String userAccount;

    private String captcha;

    /**
    * 用户密码
    */
    @SensitiveStrategyPassword
    private String userPassword;
}
