package com.caixy.adminSystem.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable
{
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 用户手机号(后期允许拓展区号和国际号码）
     */
    private String userPhone;
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户邮箱
     */
    private String userEmail;


    /**
     * 确认密码
     */
    private String checkPassword;
    private static final long serialVersionUID = 3191241716373120793L;
}
