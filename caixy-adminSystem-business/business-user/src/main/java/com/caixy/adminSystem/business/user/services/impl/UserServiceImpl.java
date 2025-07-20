package com.caixy.adminSystem.business.user.services.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caixy.adminSystem.business.user.services.UserService;
import com.caixy.adminSystem.business.user.infrastructure.convertor.UserConvertor;
import com.caixy.adminSystem.common.api.file.dto.FileUploadAfterActionResult;
import com.caixy.adminSystem.common.api.file.enums.FileAccessLevelEnum;
import com.caixy.adminSystem.common.api.file.facade.FileActionHelper;
import com.caixy.adminSystem.common.api.user.dto.UserModifyPasswordRequest;
import com.caixy.adminSystem.common.api.user.dto.UserQueryRequest;
import com.caixy.adminSystem.common.api.user.dto.UserRegisterRequest;
import com.caixy.adminSystem.common.api.user.dto.UserResetEmailRequest;
import com.caixy.adminSystem.business.user.domain.entity.User;
import com.caixy.adminSystem.common.api.user.enums.UserGenderEnum;
import com.caixy.adminSystem.business.user.infrastructure.mapper.UserMapper;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.base.constant.CommonConstant;
import com.caixy.adminSystem.common.base.constant.UserConstant;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ThrowUtils;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.common.base.utils.*;
import com.caixy.adminSystem.common.email.domain.enums.EmailCaptchaBizEnum;
import com.caixy.adminSystem.common.email.domain.models.captcha.EmailCaptchaConstant;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadFileDTO;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadFileRequest;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import com.caixy.adminSystem.infrastructure.file.manager.annotation.FileUploadActionTarget;
import com.caixy.adminSystem.infrastructure.file.strategy.FileActionStrategy;
import com.caixy.adminSystem.infrastucture.cache.redis.RedisManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
@AllArgsConstructor
@Slf4j
@FileUploadActionTarget(FileActionBizEnum.USER_AVATAR)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, FileActionStrategy
{
    private static final UserConvertor userConvertor = UserConvertor.INSTANCE;
    private final RedisManager redisManager;

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
    public UserVO getUserVO(User user)
    {
        if (user == null)
        {
            return null;
        }
        return userConvertor.toVO(user);
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
        // 检查密码是否合法
        ThrowUtils.throwIf(!RegexUtils.validatePassword(userPassword), ErrorCode.PARAMS_ERROR, "密码不符合要求");

        // 查询用户
        User currenUser = this.getById(userId);
        if (currenUser == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 校验邮箱验证码
        verifyEmailCaptcha(userModifyPasswordRequest.getCaptchaCode(), currenUser.getUserEmail(), EmailCaptchaBizEnum.RESET_PASSWORD);

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

    /**
     * 头像文件上传处理逻辑
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/6/7 下午4:31
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadAfterActionResult doAfterUploadAction(UploadContext uploadContext, FileActionHelper helper,
                                                           Path savePath, UploadFileRequest req,
                                                           HttpServletRequest servletReq)
    {
        UploadFileDTO dto = uploadContext.getUploadFileDTO();
        Long userId = dto.getUserId();
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "更新用户不存在");
        String newUrl = dto.getFileSaveInfo().getFileURL();
        if (StringUtils.isBlank(newUrl)) {
            log.error("更新用户头像失败, 访问链接失败, 用户Id: {}, 新头像链接: {}, 上传", user.getId(), newUrl);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }

        return buildAvatarUpdateResult(user, newUrl, servletReq);
    }

    private FileUploadAfterActionResult buildAvatarUpdateResult(User user, String newUrl, HttpServletRequest request)
    {
        // 更新用户头像信息
        user.setUserAvatar(newUrl);
        boolean updated = this.updateById(user);
        if (!updated) {
            log.error("更新用户头像链接失败, 用户Id: {}, 新头像链接: {}", user.getId(), newUrl);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户头像失败");
        }
        // 更新session内的头像信息
        setUserInfoInSession(user, request);
        // 设置操作返回结果
        return FileUploadAfterActionResult.successBuilder().visitUrl(newUrl).bizId(user.getId())
                                          .accessLevelEnum(FileAccessLevelEnum.PUBLIC)
                                          .displayName(String.format("avatar_%s", user.getId())).build();
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

    @Override
    public Boolean resetEmail(Long id, UserResetEmailRequest userResetEmailRequest, HttpServletRequest request)
    {
        // 从Session内获取新的邮箱值
        String newEmail = ServletUtils.getAttributeFromSession(EmailCaptchaBizEnum.RESET_EMAIL.getKey(), String.class)
                                      .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无效请求"));
        // 获取用户信息
        User userInfo = getById(id);
        if (userInfo == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // 检查新旧邮箱是否一致
        if (Objects.equals(userInfo.getUserEmail(), newEmail))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新旧邮箱不能一致");
        }
        // 检查用户密码是否正确
        boolean matchPassword = EncryptionUtils.matchPassword(userResetEmailRequest.getPassword(),
                userInfo.getUserPassword());
        if (!matchPassword)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 校验邮箱验证码
        verifyEmailCaptcha(userResetEmailRequest.getCode(), newEmail, EmailCaptchaBizEnum.RESET_EMAIL);
        // 更新邮箱
        userInfo.setUserEmail(newEmail);
        boolean updated = this.updateById(userInfo);
        if (updated)
        {
            // 删除缓存
            redisManager.delete(EmailCaptchaBizEnum.RESET_EMAIL, newEmail);
            // 清除登录状态-需要重新登录
            ServletUtils.removeAttributeInSession(UserConstant.USER_LOGIN_STATE);
            // 清除验证码签名
            ServletUtils.removeAttributeInSession(EmailCaptchaBizEnum.RESET_EMAIL.getKey());
            return true;
        }
        return false;
    }

    private void verifyEmailCaptcha(String code, String newEmail, EmailCaptchaBizEnum senderEnum)
    {
        // 从缓存获取信息
        HashMap<String, Object> captchaInCache = redisManager.getHashMap(senderEnum, newEmail);
        if (captchaInCache == null || captchaInCache.isEmpty())
        {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无效请求");
        }
        String captcha = MapUtils.safetyGetValueByKey(captchaInCache, EmailCaptchaConstant.CACHE_KEY_CODE,
                String.class);

        if (StringUtils.isBlank(captcha))
        {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无效请求");
        }

        // 验证码验证
        if (!captcha.equals(code))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
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

    private void setUserInfoInSession(User user, @NotNull HttpServletRequest request)
    {
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userConvertor.toVO(user));
    }
}
