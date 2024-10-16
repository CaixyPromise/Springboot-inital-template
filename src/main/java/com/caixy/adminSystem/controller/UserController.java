package com.caixy.adminSystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caixy.adminSystem.annotation.AuthCheck;
import com.caixy.adminSystem.common.BaseResponse;
import com.caixy.adminSystem.common.DeleteRequest;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.common.ResultUtils;
import com.caixy.adminSystem.config.WxOpenConfig;
import com.caixy.adminSystem.constant.CommonConstant;
import com.caixy.adminSystem.constant.RegexPatternConstants;
import com.caixy.adminSystem.constant.UserConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.exception.ThrowUtils;
import com.caixy.adminSystem.factory.OAuthFactory;
import com.caixy.adminSystem.model.dto.oauth.OAuthResultResponse;
import com.caixy.adminSystem.model.dto.oauth.github.GithubGetAuthorizationUrlRequest;
import com.caixy.adminSystem.model.dto.user.*;
import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.model.enums.OAuthProviderEnum;
import com.caixy.adminSystem.model.enums.UserRoleEnum;
import com.caixy.adminSystem.model.vo.user.*;
import com.caixy.adminSystem.service.UserService;
import com.caixy.adminSystem.utils.RegexUtils;
import com.caixy.adminSystem.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController
{
    @Resource
    private UserService userService;

    @Resource
    private WxOpenConfig wxOpenConfig;

    @Resource
    private OAuthFactory oAuthFactory;

    // region 登录相关
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
                userService.doOAuthLogin(oAuthResultResponse, providerEnum, request);
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
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Boolean> userRegister(@RequestBody UserRegisterRequest userRegisterRequest)
    {
        if (userRegisterRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 执行注册操作
        long saveResult = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(saveResult > 0);
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
        return ResultUtils.success(userService.userLogin(userLoginRequest, request));
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
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 实时系统中，用于获取当前登录状态的用户
     *
     * @param request
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request)
    {
        UserVO user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion


    // region 微信操作
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
            return ResultUtils.success(userService.userLoginByMpOpen(userInfo, request));
        }
        catch (Exception e)
        {
            log.error("userLoginByWxOpen error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
        }
    }


    // endregion

    // region 管理员增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<AddUserVO> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request)
    {
        // 检查请求信息
        if (userAddRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 参数校验
        String userAccount = user.getUserAccount();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        user.setUserName(userAddRequest.getUserName());
        // 生成默认密码
        String defaultPassword = userService.generatePassword();
        user.setUserPassword(defaultPassword);
        // 创建
        Long resultId = userService.doRegister(user);
        // 返回结果
        AddUserVO resultAddUserInfo = AddUserVO.builder()
                                               .userName(userAddRequest.getUserName())
                                               .userAccount(userAddRequest.getUserAccount())
                                               .userPassword(defaultPassword)
                                               .id(resultId)
                                               .build();
        return ResultUtils.success(resultAddUserInfo);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request)
    {
        if (deleteRequest == null || deleteRequest.getId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request)
    {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        userService.validUserInfo(user, false);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request)
    {
        if (id <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    // endregion

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request)
    {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody
                                                   UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request)
    {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody
                                                       UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request)
    {
        if (userQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion


    @GetMapping("/get/me")
    public BaseResponse<AboutMeVO> getMe(HttpServletRequest request)
    {
        UserVO loginUser = userService.getLoginUser(request);
        User currentUser = userService.getById(loginUser.getId());

        return ResultUtils.success(AboutMeVO.of(currentUser));
    }

    @PostMapping("/modify/password")
    public BaseResponse<Boolean> modifyPassword(@RequestBody
                                                UserModifyPasswordRequest userModifyPasswordRequest,
                                                HttpServletRequest request)
    {
        if (userModifyPasswordRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserVO loginUser = userService.getLoginUser(request);
        Boolean result = userService.modifyPassword(loginUser.getId(), userModifyPasswordRequest);
        // 如果修改成功，修改登录状态
        if (result)
        {
            ServletUtils.removeAttributeInSession(UserConstant.USER_LOGIN_STATE, request);
        }
        return ResultUtils.success(result);
    }

    /**
     * 更新个人信息
     *
     * @param userUpdateProfileRequest
     * @param request
     * @return
     */
    @PostMapping("/update/me")
    public BaseResponse<Boolean> updateMeProfile(@RequestBody
                                                     UserUpdateProfileRequest userUpdateProfileRequest,
                                          HttpServletRequest request)
    {
        if (userUpdateProfileRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateProfileRequest, user);
        user.setId(loginUser.getId());
        userService.validUserInfo(user, false);
        boolean result = userService.updateUserAndSessionById(user, request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/reset/email")
    public BaseResponse<Boolean> resetEmail(@RequestBody @Valid UserResetEmailRequest userResetEmailRequest, HttpServletRequest request)
    {
        if (userResetEmailRequest == null ||
            StringUtils.isAnyBlank(userResetEmailRequest.getPassword(), userResetEmailRequest.getCode()))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO loginUser = userService.getLoginUser(request);
        Boolean result = userService.resetEmail(loginUser.getId(), userResetEmailRequest, request);
        return ResultUtils.success(result);
    }
    /**
     * 获取加密后的邮箱数据
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/14 下午10:16
     */
    @GetMapping("/get/encrypt/info")
    public BaseResponse<EncryptAccountVO> getEncryptEmailInfo(HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        String encryptedEmail = RegexUtils.encryptText(loginUser.getUserEmail(), RegexPatternConstants.EMAIL_ENCRYPT_REGEX,
                "$1****$2");
        String encryptedPhone = RegexUtils.encryptText(loginUser.getUserPhone(), RegexPatternConstants.PHONE_ENCRYPT_REGEX,
                "$1****$2");
        return ResultUtils.success(new EncryptAccountVO(encryptedEmail, encryptedPhone));

    }
}
