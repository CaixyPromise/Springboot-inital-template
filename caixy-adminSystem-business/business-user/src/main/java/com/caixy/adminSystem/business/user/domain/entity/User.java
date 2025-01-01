package com.caixy.adminSystem.business.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.caixy.adminSystem.infrastructure.datasource.domain.entity.BaseEntity;
import com.github.houbb.sensitive.annotation.strategy.SensitiveStrategyEmail;
import com.github.houbb.sensitive.annotation.strategy.SensitiveStrategyPassword;
import com.github.houbb.sensitive.annotation.strategy.SensitiveStrategyPhone;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity
{
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    @SensitiveStrategyPassword
    private String userPassword;

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
    private String userRole;

}