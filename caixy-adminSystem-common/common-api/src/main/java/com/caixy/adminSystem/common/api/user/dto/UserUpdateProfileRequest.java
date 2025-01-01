package com.caixy.adminSystem.common.api.user.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 用户更新个人信息请求
 */
@Data
public class UserUpdateProfileRequest implements Serializable
{

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private Integer userGender;

    /**
     * 简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}