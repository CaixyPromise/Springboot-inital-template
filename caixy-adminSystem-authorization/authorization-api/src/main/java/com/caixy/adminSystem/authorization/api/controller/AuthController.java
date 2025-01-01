package com.caixy.adminSystem.authorization.api.controller;


import com.caixy.adminSystem.authorization.api.oAuth.domain.dto.github.GithubGetAuthorizationUrlRequest;
import com.caixy.adminSystem.authorization.api.oAuth.factory.OAuthFactory;
import com.caixy.adminSystem.authorization.service.manager.AuthManager;
import com.caixy.adminSystem.common.api.oauth.enums.OAuthProviderEnum;
import com.caixy.adminSystem.common.api.oauth.enums.OAuthResultResponse;
import com.caixy.adminSystem.common.api.user.dto.UserLoginRequest;
import com.caixy.adminSystem.common.api.user.vo.LoginUserVO;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.api.wx.dto.WxOAuth2LoginDTO;
import com.caixy.adminSystem.common.api.wx.service.WxFacadeService;
import com.caixy.adminSystem.common.base.constant.CommonConstant;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.response.Result;
import com.caixy.adminSystem.common.base.response.ResultUtils;
import com.caixy.adminSystem.common.base.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * 授权接口控制器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.controller.AuthController
 * @since 2024/10/17 18:04
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController
{
    private final AuthManager authManager;

    private final WxFacadeService wxFacadeService;

    private final OAuthFactory oAuthFactory;

    @GetMapping("/oauth2/{provider}/login")
    public Result<String> initOAuthLogin(
            @PathVariable String provider,
            @ModelAttribute GithubGetAuthorizationUrlRequest authorizationUrlRequest,
            HttpServletRequest request)
    {
        OAuthProviderEnum providerEnum = OAuthProviderEnum.getProviderEnum(provider);
        if (providerEnum == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的OAuth2登录方式");
        }
        authorizationUrlRequest.setSessionId(request.getSession().getId());
        log.info("authorizationUrlRequest:{}", authorizationUrlRequest);
        String authorizationUrl = oAuthFactory.getOAuth2ActionStrategy(providerEnum).getAuthorizationUrl(
                authorizationUrlRequest);
        return ResultUtils.success(authorizationUrl);
    }

    @GetMapping("/oauth2/{provider}/callback")
    public void oAuthLoginCallback(
            @PathVariable("provider") String provider,
            @RequestParam Map<String, Object> allParams,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        allParams.put("sessionId", request.getSession().getId());
        try
        {
            OAuthProviderEnum providerEnum = OAuthProviderEnum.getProviderEnum(provider);
            if (providerEnum == null)
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的OAuth2登录方式");
            }
            OAuthResultResponse oAuthResultResponse = oAuthFactory.doAuth(providerEnum, allParams);
            LoginUserVO doOAuthLogin = authManager.doOAuthLogin(oAuthResultResponse, providerEnum);
            if (doOAuthLogin != null)
            {
                response.sendRedirect(oAuthResultResponse.getRedirectUrl());
            }
            else {
                response.sendRedirect(CommonConstant.FRONTED_URL);
            }
        }
        catch (Exception e)
        {
            response.sendRedirect(CommonConstant.FRONTED_URL);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<Boolean> userLogout()
    {
        boolean result = authManager.userLogout();
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                         HttpServletRequest request)
    {
        if (userLoginRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(authManager.userLogin(userLoginRequest));
    }

    @GetMapping("/get/login")
    public Result<LoginUserVO> getLoginUser()
    {
        UserVO user = authManager.getLoginUser();
        return ResultUtils.success(authManager.getLoginUserVO(user));
    }

    @GetMapping("/login/wx_open")
    public Result<LoginUserVO> userLoginByWxOpen(@RequestParam("code") String code)
    {
        try
        {
            WxOAuth2LoginDTO userInfoByWxOpen = wxFacadeService.getUserInfoByWxOpen(code);
            if (!Optional.ofNullable(userInfoByWxOpen).isPresent()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
            }
            return ResultUtils.success(authManager.userLoginByMpOpen(userInfoByWxOpen));
        }
        catch (Exception e)
        {
            log.error("userLoginByWxOpen error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
        }
    }
}
