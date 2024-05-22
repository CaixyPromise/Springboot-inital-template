package com.caixy.adminSystem.model.vo.user;

import com.caixy.adminSystem.constant.RegexPatternConstants;
import com.caixy.adminSystem.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

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

    public static AboutMeVO of(User currentUser)
    {
        AboutMeVO aboutMeVO = new AboutMeVO();
        BeanUtils.copyProperties(currentUser, aboutMeVO);
        // 脱密处理
        aboutMeVO.setUserPhone(currentUser.getUserPhone().replaceAll(RegexPatternConstants.PHONE_ENCRYPT_REGEX, "$1****$2"));

        aboutMeVO.setUserEmail(currentUser.getUserEmail().replaceAll(RegexPatternConstants.EMAIL_ENCRYPT_REGEX, "$1****$2"));
        return aboutMeVO;
    }


    private static final long serialVersionUID = 1L;
}
