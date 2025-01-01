package com.caixy.adminSystem.common.api.user.vo;

import com.caixy.adminSystem.common.base.constant.RegexPatternConstants;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Optional;

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
     * 用户手机
     */
    private String userPhone;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 用户性别
     */
    private Integer userGender;

    /**
     * 用户头像
     */
    private String userAvatar;

    private static final long serialVersionUID = 1L;
}
