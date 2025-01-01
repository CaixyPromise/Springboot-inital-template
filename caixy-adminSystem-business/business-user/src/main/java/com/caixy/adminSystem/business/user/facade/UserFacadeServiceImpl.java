package com.caixy.adminSystem.business.user.facade;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.caixy.adminSystem.business.user.domain.convertor.UserConvertor;
import com.caixy.adminSystem.business.user.domain.entity.User;
import com.caixy.adminSystem.business.user.domain.mapper.UserMapper;
import com.caixy.adminSystem.common.api.oauth.enums.OAuthProviderEnum;
import com.caixy.adminSystem.common.api.oauth.enums.OAuthResultResponse;
import com.caixy.adminSystem.common.api.user.dto.UserLoginByOAuthAdapter;
import com.caixy.adminSystem.common.api.user.dto.UserLoginRequest;
import com.caixy.adminSystem.common.api.user.service.UserFacadeService;
import com.caixy.adminSystem.common.api.user.vo.LoginUserVO;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.api.wx.dto.WxOAuth2LoginDTO;
import com.caixy.adminSystem.common.base.constant.UserConstant;
import com.caixy.adminSystem.common.base.constant.UserRoleEnum;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.utils.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户外部调用服务实现类
 *
 * @Author CAIXYPROMISE
 * @since 2024/12/26 2:43
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserFacadeServiceImpl implements UserFacadeService
{
    private final UserMapper userMapper;
    private static final UserConvertor userConvertor = UserConvertor.INSTANCE;

    @Override
    public UserVO findUserById(Long userId)
    {
        User user = userMapper.selectById(userId);
        if (user == null)
        {
            return null;
        }
        return userConvertor.toVO(user);
    }

    @Override
    public UserVO findUserByUserAccount(String userAccount)
    {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User user = userMapper.selectOne(queryWrapper);
        return userConvertor.toVO(user);
    }

    @Override
    public List<UserVO> findUserByIds(Collection<Long> userIds)
    {
        List<User> userList = userMapper.selectBatchIds(userIds);
        if (CollUtil.isEmpty(userList))
        {
            return Collections.emptyList();
        }
        return userConvertor.toVOList(userList);
    }

    @Override
    public LoginUserVO doRegister(UserLoginRequest userLoginRequest)
    {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userLoginRequest.getUserAccount());
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null)
        {
            log.error("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        if (!EncryptionUtils.matchPassword(userLoginRequest.getUserPassword(), user.getUserPassword()))
        {
            log.error("userINFO: {}", user);
            log.error("user login failed, userAccount cannot match userPassword. userAccount: {}",
                    userLoginRequest.getUserAccount());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 检查是否被封号
        if (user.getUserRole().equals(UserConstant.BAN_ROLE))
        {
            log.info("user login failed, userAccount is ban: {}", userLoginRequest.getUserAccount());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已被封号");
        }
        return userConvertor.toLoginVO(user);
    }

    @Override
    public LoginUserVO doRegisterByOAuth(OAuthResultResponse resultResponse, OAuthProviderEnum oAuthProviderEnum)
    {
        UserLoginByOAuthAdapter loginAdapter = resultResponse.getLoginAdapter();
        UserVO oauthUserInfo = loginAdapter.getUserInfo();
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
                    userConvertor.voToEntity(oauthUserInfo),
                    userInfo,
                    new HashSet<>(Arrays.asList(
                            "id",
                            "userPassword",
                            "createTime",
                            "updateTime",
                            "isDelete",
                            "userRole")),
                    ((sourceValue, targetValue) -> sourceValue != null && targetValue == null));
        }
        if (isRegister)  // 用户不存在，则注册用户
        {
            // 注册时，设置为默认用户
            userInfo.setUserRole(UserRoleEnum.USER.getValue());
            boolean saved = userMapper.insert(userInfo) > 0;
            if (!saved) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册新用户失败");
            }
        }
        else // 更新用户信息
        {
            userMapper.updateById(userInfo);
        }
        return userConvertor.toLoginVO(userInfo);
    }

    @Override
    public LoginUserVO doRegisterByMpOpen(WxOAuth2LoginDTO wxOAuth2LoginDTO)
    {
        // 查询用户是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("unionId", wxOAuth2LoginDTO.getUnionId());
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
            user.setUnionId(wxOAuth2LoginDTO.getUnionId());
            user.setMpOpenId(wxOAuth2LoginDTO.getMpOpenId());
            user.setUserAvatar(wxOAuth2LoginDTO.getUserAvatar());
            user.setUserName(wxOAuth2LoginDTO.getNickName());
            int result = userMapper.insert(user);
            if (result == 0)
            {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
            }
        }
        return userConvertor.toLoginVO(user);
    }
}
