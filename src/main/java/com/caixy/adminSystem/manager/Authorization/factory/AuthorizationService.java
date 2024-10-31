package com.caixy.adminSystem.manager.Authorization.factory;

import com.caixy.adminSystem.model.convertor.user.UserConvertor;
import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.model.enums.UserRoleEnum;
import com.caixy.adminSystem.model.vo.user.LoginUserVO;
import com.caixy.adminSystem.model.vo.user.UserVO;
import com.caixy.adminSystem.utils.NetUtils;
import com.caixy.adminSystem.utils.ServletUtils;
import com.caixy.adminSystem.utils.UserAgentUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 验证服务类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Authorization.factory.AuthService
 * @since 2024/10/27 01:20
 */
public interface AuthorizationService
{
    /**
     * 服务名称
     */
    String getName();

    /**
     * 基于servlet获取检查登录
     */
    Boolean checkLogin(HttpServletRequest request);

    /**
     * 检查是否登录
     */
    Boolean checkLogin();

    /**
     * 是否是某个身份
     */
    Boolean asRole(UserRoleEnum roleEnum);

    /**
     * 获取登录信息，如果未登录则抛出异常
     */
    void checkLoginOrThrow();

    /**
     * 登录操作
     */
    LoginUserVO doLogin(User user, HttpServletRequest request);

    /**
     * 登出操作
     *
     * @return
     */
    boolean doLogout();

    /**
     * 获取用户信息，存在时检查是否到过期时间，过期自动更换token
     * 不存在时抛出错误
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/27 下午8:17
     */
    UserVO getLoginUser(HttpServletRequest request);

    /**
     * 获取用户登录信息
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/27 下午8:25
     */
    default UserVO getLoginUser()
    {
        return getLoginUser(ServletUtils.getRequest());
    }


    /**
     * 获取登录信息，如果未登录则返回null
     */
    UserVO getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否是管理员
     */
    Boolean isAdmin();


    /**
     * 获取所有已登录用户的信息，支持分页
     *
     * @param currentSize 页码，从1开始
     * @param size        每页大小
     * @return 用户列表
     */
    List<UserVO> getAllLoggedInUsers(int currentSize, int size);

    /**
     * 获取已登录用户数量
     */
    int getLoggedInUserCount();

    /**
     * 强制某用户下线
     */
    void forceLogout(Long userId);

    /**
     * 给前端传递的用户VO
     */
    default LoginUserVO getLoginUserVO(UserVO userVO)
    {
        if (userVO == null)
        {
            return null;
        }
        return UserConvertor.INSTANCE.voToLoginVO(userVO);
    }

    default void setUserLoginInfo(UserVO userVO, HttpServletRequest request)
    {
        if (userVO == null)
        {
            return;
        }
        userVO.setLoginTime(System.currentTimeMillis());

        // 设置登录信息
        String ip = NetUtils.getIpAddress(request);
        userVO.setLoginIp(ip);
        userVO.setLoginLocation(NetUtils.getRealAddressByIP(ip));
        userVO.setBrowser(UserAgentUtils.getBrowser(request));
        userVO.setOs(UserAgentUtils.getOS(request));
        userVO.setSessionId(request.getSession().getId());
    }
}
