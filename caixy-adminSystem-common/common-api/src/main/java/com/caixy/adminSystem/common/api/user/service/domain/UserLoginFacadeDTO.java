package com.caixy.adminSystem.common.api.user.service.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录数据传输对象
 *
 * @Author CAIXYPROMISE
 * @since 2024/12/28 17:15
 */
@Data
@Builder
public class UserLoginFacadeDTO implements Serializable
{
    /**
    * 用户账号
    */
    private String userAccount;
    /**
    * 用户密码
    */
    private String userPassword;
    
    private static final long serialVersionUID = 1L;
}
