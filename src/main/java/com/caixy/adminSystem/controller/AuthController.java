package com.caixy.adminSystem.controller;

import com.caixy.adminSystem.common.BaseResponse;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.common.ResultUtils;
import com.caixy.adminSystem.config.WxOpenConfig;
import com.caixy.adminSystem.constant.CommonConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.factory.OAuthFactory;
import com.caixy.adminSystem.manager.Authorization.AuthManager;
import com.caixy.adminSystem.model.dto.oauth.OAuthResultResponse;
import com.caixy.adminSystem.model.dto.oauth.github.GithubGetAuthorizationUrlRequest;
import com.caixy.adminSystem.model.dto.user.UserLoginRequest;
import com.caixy.adminSystem.model.enums.OAuthProviderEnum;
import com.caixy.adminSystem.model.vo.user.LoginUserVO;
import com.caixy.adminSystem.model.vo.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import com.caixy.adminSystem.utils.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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

    private final WxOpenConfig wxOpenConfig;

    private final OAuthFactory oAuthFactory;

    @GetMapping("/oauth2/{provider}/login")
    public BaseResponse<String> initOAuthLogin(
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
            if (oAuthResultResponse.isSuccess())
            {
                authManager.doOAuthLogin(oAuthResultResponse, providerEnum, request);
            }
            response.sendRedirect(oAuthResultResponse.getRedirectUrl());
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
    public BaseResponse<Boolean> userLogout(HttpServletRequest request)
    {
        if (request == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = authManager.userLogout(request);
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
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
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
        return ResultUtils.success(authManager.userLogin(userLoginRequest, request));
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request)
    {
        UserVO user = authManager.getLoginUser(request);
        return ResultUtils.success(authManager.getLoginUserVO(user));
    }

    @GetMapping("/login/wx_open")
    public BaseResponse<LoginUserVO> userLoginByWxOpen(HttpServletRequest request, HttpServletResponse response,
                                                       @RequestParam("code") String code)
    {
        WxOAuth2AccessToken accessToken;
        try
        {
            WxMpService wxService = wxOpenConfig.getWxMpService();
            accessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
            String unionId = userInfo.getUnionId();
            String mpOpenId = userInfo.getOpenid();
            if (StringUtils.isAnyBlank(unionId, mpOpenId))
            {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
            }
            return ResultUtils.success(authManager.userLoginByMpOpen(userInfo, request));
        }
        catch (Exception e)
        {
            log.error("userLoginByWxOpen error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
        }
    }
}
