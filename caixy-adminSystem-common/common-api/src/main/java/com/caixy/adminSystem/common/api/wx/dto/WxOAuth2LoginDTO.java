package com.caixy.adminSystem.common.api.wx.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信OAuth2登录信息对象
 *
 * @Author CAIXYPROMISE
 * @since 2024/12/28 16:43
 */
@Data
@Builder
public class WxOAuth2LoginDTO implements Serializable
{
   /**
    * 用户唯一标识
    */
    private String unionId;
    /**
    * openid
    */
    private String mpOpenId;

    /**
    * 用户头像
    */
    private String userAvatar;

    /**
    * 用户昵称
    */
    private String nickName;


    private static final long serialVersionUID = 1L;
}
