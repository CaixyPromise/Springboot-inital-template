package com.caixy.adminSystem.model.vo.user;

import java.io.Serializable;
import java.util.Date;

import com.caixy.adminSystem.model.enums.UserRoleEnum;
import lombok.Data;

/**
 * 用户视图（脱敏）
 *
 
 */
@Data
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
    private String userPhone;

    /**
     * 用户邮箱
     */
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

    private static final long serialVersionUID = 1L;
}