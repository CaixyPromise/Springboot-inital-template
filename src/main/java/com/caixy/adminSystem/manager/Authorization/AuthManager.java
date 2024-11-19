package com.caixy.adminSystem.manager.Authorization;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.constant.UserConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.exception.ThrowUtils;
import com.caixy.adminSystem.manager.Authorization.factory.AuthorizationService;
import com.caixy.adminSystem.mapper.UserMapper;
import com.caixy.adminSystem.model.convertor.user.UserConvertor;
import com.caixy.adminSystem.model.dto.oauth.OAuthResultResponse;
import com.caixy.adminSystem.model.dto.user.UserLoginByOAuthAdapter;
import com.caixy.adminSystem.model.dto.user.UserLoginRequest;
import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.model.enums.OAuthProviderEnum;
import com.caixy.adminSystem.model.enums.UserRoleEnum;
import com.caixy.adminSystem.model.vo.user.LoginUserVO;
import com.caixy.adminSystem.model.vo.user.UserVO;
import com.caixy.adminSystem.service.CaptchaService;
import com.caixy.adminSystem.utils.EncryptionUtils;
import com.caixy.adminSystem.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
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
    private final UserMapper userMapper;

    private final CaptchaService captchaService;

    private static final UserConvertor userConvertor = UserConvertor.INSTANCE;

    private final AuthorizationService authorizationService;

    public AuthManager(UserMapper userMapper, CaptchaService captchaService, AuthorizationService authorizationService)
    {
        this.userMapper = userMapper;
        this.captchaService = captchaService;
        this.authorizationService = authorizationService;
        log.info("创建验证服务成功，系统默认登录验证服务: {}", authorizationService.getName());
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
        return authorizationService.checkLogin();
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
        return authorizationService.getLoginUser();
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
        return authorizationService.getLoginUserPermitNull(ServletUtils.getRequest());
    }

    public LoginUserVO getLoginUserVO(UserVO user)
    {
        if (user == null)
        {
            return null;
        }
        return userConvertor.voToLoginVO(user);
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
        return authorizationService.isAdmin();
    }

    public boolean isAdmin(UserVO user)
    {
        return user != null && UserRoleEnum.ADMIN.equals(user.getUserRole());
    }

    public boolean userLogout()
    {
        return authorizationService.doLogout();
    }

    private LoginUserVO doLogin(User user)
    {
        return authorizationService.doLogin(user, ServletUtils.getRequest());
    }

    /**
     * 基于公众号的登录
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/28 上午1:44
     */
    public LoginUserVO userLoginByMpOpen(@NotNull WxOAuth2UserInfo wxOAuth2UserInfo)
    {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern())
        {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = userMapper.selectOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole()))
            {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null)
            {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                int result = userMapper.insert(user);
                if (result == 0)
                {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            return doLogin(user);
        }
    }

    public Page<UserVO> getOnlineUsers(int current, int size)
    {
        if (current <= 0 || size <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数错误");
        }
        List<UserVO> allLoggedInUsers = authorizationService.getAllLoggedInUsers(current, size);
        Page<UserVO> pageInfo = new Page<>(current, size);
        pageInfo.setRecords(allLoggedInUsers);
        pageInfo.setTotal(authorizationService.getLoggedInUserCount());
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
        authorizationService.forceLogout(userId);
    }

    /**
     * 基于第三方OAuth登录
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/28 上午1:45
     */
    public Boolean doOAuthLogin(@NotNull OAuthResultResponse resultResponse,
                                @NotNull OAuthProviderEnum providerEnum)
    {
        if (!resultResponse.isSuccess())
        {
            return false;
        }
        UserLoginByOAuthAdapter loginAdapter = resultResponse.getLoginAdapter();
        User oauthUserInfo = loginAdapter.getUserInfo();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(loginAdapter.getUniqueFieldName(), loginAdapter.getUniqueFieldValue());
        User userInfo = userMapper.selectOne(queryWrapper);
        log.info("查询到登录用户信息: {}", userInfo);
        // 如果未查询到，注册该用户
        boolean isRegister = userInfo == null;
        if (isRegister)
        {
            userInfo = new User();
            userConvertor.copyAllPropertiesIgnoringId(oauthUserInfo, userInfo);
        }
        else
        {
            userInfo = userConvertor.copyPropertiesWithStrategy(
                    oauthUserInfo,
                    userInfo,
                    new HashSet<>(
                            Arrays.asList("id", "userPassword", "createTime", "updateTime", "isDelete", "userRole")),
                    ((sourceValue, targetValue) -> sourceValue != null && targetValue == null));

        }
        if (isRegister)  // 用户不存在，则注册用户
        {
            // 注册时，设置为默认用户
            userInfo.setUserRole(UserRoleEnum.USER.getValue());
            return userMapper.insert(userInfo) > 0;
        }
        else // 更新用户信息
        {
            userMapper.updateById(userInfo);
            return true;
        }
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
        String userAccount = Optional.ofNullable(userLoginRequest.getUserAccount()).orElseThrow(
                () -> new BusinessException(ErrorCode.PARAMS_ERROR, "用户名为空"));
        String userPassword = Optional.ofNullable(userLoginRequest.getUserPassword()).orElseThrow(
                () -> new BusinessException(ErrorCode.PARAMS_ERROR, "密码为空"));
        String captchaCode = Optional.ofNullable(userLoginRequest.getCaptcha()).orElseThrow(
                () -> new BusinessException(ErrorCode.PARAMS_ERROR, "验证码为空"));
        // 1. 校验
        if (userAccount.length() < 4)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        // 1.2 校验验证码
        ThrowUtils.throwIf(!captchaService.verifyCaptcha(captchaCode), ErrorCode.PARAMS_ERROR,
                "验证码错误");
        // 2. 根据账号查询用户是否存在
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null)
        {
            log.error("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        if (!EncryptionUtils.matchPassword(userPassword, user.getUserPassword()))
        {
            log.error("userINFO: {}", user);
            log.error("user login failed, userAccount cannot match userPassword. userAccount: {}, userPassword: {}",
                    userAccount, userPassword);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 检查是否被封号
        if (user.getUserRole().equals(UserConstant.BAN_ROLE))
        {
            log.info("user login failed, userAccount is ban: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已被封号");
        }
        // 3. 记录用户的登录态
        return doLogin(user);
    }
}
