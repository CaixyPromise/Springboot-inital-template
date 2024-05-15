package com.caixy.adminSystem.model.vo.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 关于我个人信息VO
 *
 * @name: com.caixy.adminSystem.model.vo.user.AboutMeVO
 * @author: CAIXYPROMISE
 * @since: 2024-04-14 20:39
 **/
@Data
public class AboutMeVO implements Serializable
{
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户公司
     */
    private String userCompany;


    private static final long serialVersionUID = 1L;
}
