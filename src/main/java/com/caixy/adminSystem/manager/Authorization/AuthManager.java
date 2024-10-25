package com.caixy.adminSystem.manager.Authorization;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.constant.UserConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.exception.ThrowUtils;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static com.caixy.adminSystem.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 安全权限管理器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Authorization.AuthManager
 * @since 2024/10/17 17:21
 */
@Service
@Slf4j
@AllArgsConstructor
public class AuthManager
{
    private final UserMapper userMapper;

    private final CaptchaService captchaService;

    private static final UserConvertor userConvertor = UserConvertor.INSTANCE;

    public UserVO getLoginUser(@NotNull HttpServletRequest request)
    {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO currentUser = (UserVO) userObj;
        if (currentUser == null || currentUser.getId() == null)
        {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (currentUser.getUserRole().equals(UserRoleEnum.BAN))
        {
            // 被封号的用户，先断开连接
            userLogout(request);
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被封禁");
        }
        return currentUser;
    }



    public UserVO getLoginUserPermitNull(@NotNull HttpServletRequest request)
    {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO currentUser = (UserVO) userObj;
        if (currentUser == null || currentUser.getId() == null)
        {
            return null;
        }
        return currentUser;
    }

    public LoginUserVO getLoginUserVO(UserVO user)
    {
        if (user == null)
        {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        userConvertor.voToLoginVO(user, loginUserVO);
        return loginUserVO;
    }

    public boolean isAdmin(@NotNull HttpServletRequest request)
    {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO user = (UserVO) userObj;
        return isAdmin(user);
    }

    public boolean isAdmin(UserVO user)
    {
        return user != null && UserRoleEnum.ADMIN.equals(user.getUserRole());
    }

    public boolean userLogout(@NotNull HttpServletRequest request)
    {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    public LoginUserVO userLoginByMpOpen(@NotNull WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request)
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
            return doLogin(user, request);
        }
    }
    public LoginUserVO userLogin(@NotNull UserLoginRequest userLoginRequest, HttpServletRequest request)
    {
        // 0. 提取参数
        // 1.1 检查参数是否完整
        String userAccount = Optional.ofNullable(userLoginRequest.getUserAccount()).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "用户名为空"));
        String userPassword = Optional.ofNullable(userLoginRequest.getUserPassword()).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "密码为空"));
        String captchaCode = Optional.ofNullable(userLoginRequest.getCaptcha()).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "验证码为空"));
        String captchaId = Optional.ofNullable(userLoginRequest.getCaptchaId()).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "验证码信息为空"));
        // 1. 校验
        if (userAccount.length() < 4)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        // 1.2 校验验证码
        ThrowUtils.throwIf(captchaService.verifyCaptcha(captchaCode, captchaId, request), ErrorCode.PARAMS_ERROR,
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
        return doLogin(user, request);
    }

    private LoginUserVO doLogin(User user, HttpServletRequest request)
    {
        LoginUserVO loginUserVO = new LoginUserVO();
        userConvertor.toLoginVO(user, loginUserVO);
        setUserInfoInSession(user, request);
        return loginUserVO;
    }

    private void setUserInfoInSession(User user, @NotNull HttpServletRequest request)
    {
        UserVO userVO = new UserVO();
        userConvertor.toVO(user, userVO);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userVO);
    }

    public Boolean doOAuthLogin(@NotNull OAuthResultResponse resultResponse,
                                @NotNull OAuthProviderEnum providerEnum,
                                @NotNull HttpServletRequest request)
    {
        if (!resultResponse.isSuccess())
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证失败");
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
        userInfo.setUserRole(UserRoleEnum.USER.getValue());
        int result = isRegister ? userMapper.insert(userInfo) : userMapper.updateById(userInfo);
        log.info("UserInfo: {}", userInfo);
        if (result > 0)
        {
            doLogin(userInfo, request);
        }
        return result > 0;
    }
}
