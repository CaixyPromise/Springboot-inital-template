package com.caixy.adminSystem.common.api.user.service;

import com.caixy.adminSystem.common.api.oauth.enums.OAuthProviderEnum;
import com.caixy.adminSystem.common.api.oauth.enums.OAuthResultResponse;
import com.caixy.adminSystem.common.api.user.dto.UserLoginRequest;
import com.caixy.adminSystem.common.api.user.vo.LoginUserVO;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.api.wx.dto.WxOAuth2LoginDTO;

import java.util.Collection;
import java.util.List;

/**
 * 用户外部调用服务类
 *
 * @Author CAIXYPROMISE
 * @since 2024/12/26 2:41
 */
public interface UserFacadeService
{
    /**
    * 根据用户id获取用户信息
    */
    UserVO findUserById(Long userId);

    /**
    * 根据用户账号获取用户信息
    */
    UserVO findUserByUserAccount(String userAccount);

    /**
    * 根据用户ids获取用户信息
    */
    List<UserVO> findUserByIds(Collection<Long> userIds);

    /**
     * 密码登录
     *
     * @return
     */
    UserVO doLoginWithValidUser(UserLoginRequest userLoginRequest);

    /**
    * 第三方登录
    */
    LoginUserVO doRegisterByOAuth(OAuthResultResponse resultResponse, OAuthProviderEnum oAuthProviderEnum);

    /**
    * 公众号登录
    */
    LoginUserVO doRegisterByMpOpen(WxOAuth2LoginDTO wxOAuth2LoginDTO);
}
