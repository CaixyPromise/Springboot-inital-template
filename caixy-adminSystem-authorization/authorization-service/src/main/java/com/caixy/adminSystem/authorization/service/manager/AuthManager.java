package com.caixy.adminSystem.authorization.service.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caixy.adminSystem.authorization.service.domain.convertor.AuthUserConvertor;
import com.caixy.adminSystem.authorization.service.factory.AuthorizationFactory;
import com.caixy.adminSystem.common.api.oauth.enums.OAuthProviderEnum;
import com.caixy.adminSystem.common.api.oauth.enums.OAuthResultResponse;
import com.caixy.adminSystem.common.api.user.dto.UserLoginRequest;
import com.caixy.adminSystem.common.api.user.service.UserFacadeService;
import com.caixy.adminSystem.common.api.user.vo.LoginUserVO;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.api.wx.dto.WxOAuth2LoginDTO;
import com.caixy.adminSystem.common.base.constant.UserRoleEnum;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ThrowUtils;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.utils.StringUtils;
import com.caixy.adminSystem.common.captcha.service.CaptchaService;
import com.caixy.adminSystem.common.base.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * 安全权限管理器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Authorization.AuthManager
 * @since 2024/10/17 17:21
 */
@Service
@Slf4j
public class AuthManager
{
    private final UserFacadeService userFacadeService;

    private final CaptchaService captchaService;

    private final AuthorizationFactory authorizationFactory;

    private static final AuthUserConvertor authUserConvertor = AuthUserConvertor.INSTANCE;

    public AuthManager(UserFacadeService userFacadeService, CaptchaService captchaService, AuthorizationFactory authorizationFactory)
    {
        this.userFacadeService = userFacadeService;
        this.captchaService = captchaService;
        this.authorizationFactory = authorizationFactory;
        log.info("创建验证服务成功，系统默认登录验证服务: {}", authorizationFactory.getName());
    }

    /**
     * 检查是否登录
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/27 上午1:13
     */
    public Boolean checkLogin()
    {
        return authorizationFactory.checkLogin();
    }

    /**
     * 获取登录的用户信息
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/27 上午12:12
     */
    public UserVO getLoginUser()
    {
        return authorizationFactory.getLoginUser();
    }

    /**
     * 获取当前用户，允许不登录
     *
     * @return 当前用户信息，登录会检查是否是封号，未登录返回null
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/27 上午12:42
     */
    public UserVO getLoginUserPermitNull()
    {
        return authorizationFactory.getLoginUserPermitNull(ServletUtils.getRequest());
    }

    public LoginUserVO getLoginUserVO(UserVO user)
    {
        if (user == null)
        {
            return null;
        }
        return authUserConvertor.voToLoginVO(user);
    }

    /**
     * 是否是管理员
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/27 上午12:46
     */
    public boolean isAdmin()
    {
        return authorizationFactory.isAdmin();
    }

    public boolean isAdmin(UserVO user)
    {
        return user != null && UserRoleEnum.ADMIN.equals(user.getUserRole());
    }

    public boolean userLogout()
    {
        return authorizationFactory.doLogout();
    }

    private LoginUserVO doLogin(UserVO user)
    {
        return authorizationFactory.doLogin(user, ServletUtils.getRequest());
    }

    /**
     * 基于公众号的登录
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/28 上午1:44
     */
    public LoginUserVO userLoginByMpOpen(@NotNull WxOAuth2LoginDTO wxOAuth2UserInfo)
    {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getMpOpenId();
        if (StringUtils.isAnyBlank(unionId, mpOpenId))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        // 单机锁
        synchronized (unionId.intern())
        {
            return userFacadeService.doRegisterByMpOpen(wxOAuth2UserInfo);
        }
    }

    public Page<UserVO> getOnlineUsers(int current, int size)
    {
        if (current <= 0 || size <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数错误");
        }
        List<UserVO> allLoggedInUsers = authorizationFactory.getAllLoggedInUsers(current, size);
        Page<UserVO> pageInfo = new Page<>(current, size);
        pageInfo.setRecords(allLoggedInUsers);
        pageInfo.setTotal(authorizationFactory.getLoggedInUserCount());
        return pageInfo;
    }

    /**
     * 强制下线指定用户
     *
     * @param userId 用户ID
     */
    public void forceLogout(Long userId)
    {
        if (userId == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        authorizationFactory.forceLogout(userId);
    }

    /**
     * 基于第三方OAuth登录
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/28 上午1:45
     */
    public LoginUserVO doOAuthLogin(@NotNull OAuthResultResponse resultResponse,
                                @NotNull OAuthProviderEnum providerEnum)
    {
        return userFacadeService.doRegisterByOAuth(resultResponse, providerEnum);
    }

    /**
     * 基于系统提供的账号密码登录方式
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/28 上午1:45
     */
    public LoginUserVO userLogin(@NotNull UserLoginRequest userLoginRequest)
    {
        // 0. 提取参数
        // 1.1 检查参数是否完整
        String userAccount = Optional
                .ofNullable(userLoginRequest.getUserAccount())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "用户名为空"));
        String userPassword = Optional
                .ofNullable(userLoginRequest.getUserPassword())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "密码为空"));
        String captchaCode = Optional
                .ofNullable(userLoginRequest.getCaptcha())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "验证码为空"));
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        // 1.2 校验验证码
        ThrowUtils.throwIf(!captchaService.verifyCaptcha(captchaCode), ErrorCode.PARAMS_ERROR,
                "验证码错误");
        UserVO userVO = userFacadeService.doLoginWithValidUser(userLoginRequest);
        return authorizationFactory.doLogin(userVO, ServletUtils.getRequest());
    }
}
