package com.caixy.adminSystem.model.vo.user;

import com.caixy.adminSystem.model.enums.UserRoleEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 已登录用户视图（脱敏）
 *
 
 **/
@Data
public class LoginUserVO implements Serializable {
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户性别
     */
    private Integer userGender;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private UserRoleEnum userRole;

    private static final long serialVersionUID = 1L;
}