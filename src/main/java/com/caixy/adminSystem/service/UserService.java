package com.caixy.adminSystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caixy.adminSystem.model.dto.user.*;
import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.model.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User>
{

    /**
     * 用户注册
     *
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    //    Long makeRegister(String userAccount, String userPassword);
    Long doRegister(User user);

    String generatePassword();

    Boolean modifyPassword(Long userId, UserModifyPasswordRequest userModifyPasswordRequest);

    void validUserInfo(User user, boolean add);

    Boolean updateUserAndSessionById(User user, HttpServletRequest request);

    Boolean resetEmail(Long id, UserResetEmailRequest userResetEmailRequest, HttpServletRequest request);
}
