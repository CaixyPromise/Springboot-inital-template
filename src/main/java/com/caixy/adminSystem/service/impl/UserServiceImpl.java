package com.caixy.adminSystem.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caixy.adminSystem.manager.UploadManager.annotation.FileUploadActionTarget;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.constant.CommonConstant;
import com.caixy.adminSystem.constant.UserConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.exception.ThrowUtils;
import com.caixy.adminSystem.mapper.UserMapper;
import com.caixy.adminSystem.model.dto.file.UploadFileDTO;
import com.caixy.adminSystem.model.dto.file.UploadFileRequest;
import com.caixy.adminSystem.model.dto.oauth.OAuthResultResponse;
import com.caixy.adminSystem.model.dto.user.*;
import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.model.convertor.user.UserConvertor;
import com.caixy.adminSystem.model.enums.FileActionBizEnum;
import com.caixy.adminSystem.model.enums.OAuthProviderEnum;
import com.caixy.adminSystem.model.enums.UserGenderEnum;
import com.caixy.adminSystem.model.enums.UserRoleEnum;
import com.caixy.adminSystem.model.vo.user.LoginUserVO;
import com.caixy.adminSystem.model.vo.user.UserVO;
import com.caixy.adminSystem.service.CaptchaService;
import com.caixy.adminSystem.service.UserService;
import com.caixy.adminSystem.strategy.FileActionStrategy;
import com.caixy.adminSystem.strategy.UploadFileMethodStrategy;
import com.caixy.adminSystem.utils.EncryptionUtils;
import com.caixy.adminSystem.utils.RegexUtils;
import com.caixy.adminSystem.utils.SqlUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.caixy.adminSystem.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 */
@Service
@Slf4j
@FileUploadActionTarget(FileActionBizEnum.USER_AVATAR)
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, FileActionStrategy
{
    private final CaptchaService captchaService;
    private final UserConvertor userConvertor = UserConvertor.INSTANCE;

    @Override
    public long userRegister(@NotNull UserRegisterRequest userRegisterRequest)
    {
        String userPassword = userRegisterRequest.getUserPassword();
        User user = new User();
        BeanUtils.copyProperties(userRegisterRequest, user);
        // 1. 校验
        validUserInfo(user, true);
        // 2. 插入数据
        user.setUserPassword(userPassword);
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        return doRegister(user);
    }

    @Override
    public LoginUserVO userLogin(@NotNull UserLoginRequest userLoginRequest, HttpServletRequest request)
    {
        // 0. 提取参数
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        String captcha = userLoginRequest.getCaptcha().trim();
        String captchaId = userLoginRequest.getCaptchaId();
        // 1. 校验
        // 1.1 检查参数是否完整
        if (StringUtils.isAnyBlank(userAccount, userPassword))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        // 1.2 校验验证码
        verifyCaptchaCode(captcha, captchaId, request);

        // 2. 根据账号查询用户是否存在
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = baseMapper.selectOne(queryWrapper);
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

    @Override
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
            User user = this.getOne(queryWrapper);
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
                boolean result = this.save(user);
                if (!result)
                {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            return doLogin(user, request);
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
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

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
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

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(@NotNull HttpServletRequest request)
    {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO user = (UserVO) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(UserVO user)
    {
        return user != null && UserRoleEnum.ADMIN.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
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

    @Override
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

    @Override
    public UserVO getUserVO(User user)
    {
        if (user == null)
        {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList)
    {
        if (CollUtil.isEmpty(userList))
        {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest)
    {
        if (userQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    // 重载的 makeRegister 方法，接收 User 对象
    @Override
    public Long doRegister(@NotNull User user)
    {
        synchronized (user.getUserAccount().intern())
        {
            // 检查账户是否重复
            checkUserAccount(user.getUserAccount());

            // 加密密码并设置
            String encryptPassword = EncryptionUtils.encryptPassword(user.getUserPassword());
            user.setUserPassword(encryptPassword);

            // 插入数据
            boolean saveResult = this.save(user);
            if (!saveResult)
            {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 随机生成密码
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/4/26 下午9:42
     */
    @Override
    public String generatePassword()
    {
        // 定义字符集
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String specialCharacters = "!@#$%&*.?";

        // 确保每种字符至少出现一次
        List<Character> passwordChars = new ArrayList<>();
        passwordChars.add(RandomUtil.randomChar(lowerCaseLetters));
        passwordChars.add(RandomUtil.randomChar(upperCaseLetters));
        passwordChars.add(RandomUtil.randomChar(numbers));
        passwordChars.add(RandomUtil.randomChar(specialCharacters));

        // 随机密码长度
        int length = RandomUtil.randomInt(8, 21);

        // 填充剩余的字符
        String allCharacters = lowerCaseLetters + upperCaseLetters + numbers + specialCharacters;
        for (int i = passwordChars.size(); i < length; i++)
        {
            passwordChars.add(RandomUtil.randomChar(allCharacters));
        }

        // 打乱字符顺序
        Collections.shuffle(passwordChars);

        // 构建最终的密码字符串
        StringBuilder password = new StringBuilder();
        for (Character ch : passwordChars)
        {
            password.append(ch);
        }

        return password.toString();
    }


    @Override
    public Boolean modifyPassword(Long userId, @NotNull UserModifyPasswordRequest userModifyPasswordRequest)
    {
        String userPassword = userModifyPasswordRequest.getNewPassword();
        if (userPassword.length() < 8)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (userPassword.length() > 20)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过长");
        }
        if (!userPassword.equals(userModifyPasswordRequest.getConfirmPassword()))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }
        // 查询用户
        User currenUser = this.getById(userId);
        if (currenUser == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 校验密码
        boolean matches =
                EncryptionUtils.matchPassword(userModifyPasswordRequest.getOldPassword(), currenUser.getUserPassword());
        if (!matches)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "原密码错误");
        }

        // 加密密码
        String encryptPassword = EncryptionUtils.encryptPassword(userPassword);
        currenUser.setUserPassword(encryptPassword);
        // 清空登录状态
        return this.updateById(currenUser);
    }

    /**
     * 校验用户信息
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午3:56
     */
    @Override
    public void validUserInfo(@NotNull User user, boolean add)
    {
        if (add)
        {
            String userAccount = user.getUserAccount();
            String userPassword = user.getUserPassword();
            String userEmail = user.getUserEmail();
            String userPhone = user.getUserPhone();
            // 检查密码是否合法
            ThrowUtils.throwIf(!RegexUtils.validatePassword(userPassword), ErrorCode.PARAMS_ERROR, "密码不符合要求");
            // 检查账号是否合法
            ThrowUtils.throwIf(!RegexUtils.validateAccount(userAccount), ErrorCode.PARAMS_ERROR, "用户账号格式错误");
            // 检查手机号是否合法
            ThrowUtils.throwIf(!RegexUtils.isMobilePhone(userPhone), ErrorCode.PARAMS_ERROR, "手机号格式错误");
            // 检查用户邮箱
            ThrowUtils.throwIf(!RegexUtils.isEmail(userEmail), ErrorCode.PARAMS_ERROR, "邮箱格式错误");

            // 查询账号是否存在，同时检查手机、邮箱唯一性
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount).or();
            queryWrapper.eq("userEmail", userEmail).or();
            queryWrapper.eq("userPhone", userPhone);
            long count = baseMapper.selectCount(queryWrapper);
            // 账号已存在
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        Integer gender = user.getUserGender();
        if (gender != null && UserGenderEnum.getEnumByValue(gender) == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "性别参数错误");
        }
    }

    @Override
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
        User userInfo = this.getOne(queryWrapper);
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
        boolean result = isRegister ? this.save(userInfo) : this.updateById(userInfo);
        log.info("UserInfo: {}", userInfo);
        if (result)
        {
            doLogin(userInfo, request);
        }
        return result;
    }

    /**
     * 头像文件上传处理逻辑
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/6/7 下午4:31
     */
    @Override
    public Boolean doAfterUploadAction(@NotNull UploadFileDTO uploadFileDTO, Path savePath,
                                       UploadFileRequest uploadFileRequest) throws IOException
    {
        Long userId = uploadFileDTO.getUserId();
        User user = this.getById(userId);
        if (user == null)
        {
            return false;
        }
        String userAvatar = user.getUserAvatar();
        user.setUserAvatar(uploadFileDTO.getFileInfo().getFileURL());
        UploadFileMethodStrategy uploadManager = uploadFileDTO.getUploadManager();
        boolean updated = this.updateById(user);
        if (updated)
        {
            if (StringUtils.isNotBlank(userAvatar))
            {
                FileActionBizEnum uploadBizEnum = uploadFileDTO.getFileActionBizEnum();

                String[] filename = userAvatar.split("/");
                if (filename.length > 0)
                {
                    Path filepath = uploadBizEnum.buildFileAbsolutePathAndName(userId, filename[filename.length - 1]);
                    uploadManager.deleteFile(filepath);
                    return true;
                }
                return false;
            }
            // 可能初始化的时候没有设置头像，可以设置一个默认头像，但不允许删除默认头像
            return true;
        }
        return false;
    }

    /**
     * 更新用户信息，同时更新Session内的信息
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/9/30 上午2:01
     */
    @Override
    public Boolean updateUserAndSessionById(User user, HttpServletRequest request)
    {
        boolean result = this.updateById(user);
        if (result)
        {
            setUserInfoInSession(getById(user.getId()), request);
        }
        return result;
    }

    // 私有方法，用于检查账户是否重复
    private void checkUserAccount(String userAccount)
    {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
    }

    private void verifyCaptchaCode(String captchaCode, String captchaId, HttpServletRequest request)
    {
        ThrowUtils.throwIf(captchaService.verifyCaptcha(captchaCode, captchaId, request), ErrorCode.PARAMS_ERROR,
                "验证码错误");
    }

    @NotNull
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
}
