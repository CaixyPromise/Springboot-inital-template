package com.caixy.adminSystem.common.api.user.vo;

import com.caixy.adminSystem.common.base.constant.UserRoleEnum;
import com.github.houbb.sensitive.annotation.strategy.SensitiveStrategyEmail;
import com.github.houbb.sensitive.annotation.strategy.SensitiveStrategyIp;
import com.github.houbb.sensitive.annotation.strategy.SensitiveStrategyPhone;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图（脱敏）
 *
 
 */
@Data
@Builder
public class UserVO implements Serializable
{

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 微信开放平台id
     */
    private String unionId;

    /**
     * github用户Id
     */
    private Long githubId;

    /**
     * github用户名
     */
    private String githubUserName;

    /**
     * 用户手机号(后期允许拓展区号和国际号码）
     */
    @SensitiveStrategyPhone
    private String userPhone;

    /**
     * 用户邮箱
     */
    @SensitiveStrategyEmail
    private String userEmail;

    /**
     * 公众号openId
     */
    private String mpOpenId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户性别
     */
    private Integer userGender;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * 登录IP
     */
    @SensitiveStrategyIp
    private String loginIp;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 用户token
     */
    private String token;

    /**
     * 用户sessionId;
     */
    private String sessionId;

    private static final long serialVersionUID = 1L;
}