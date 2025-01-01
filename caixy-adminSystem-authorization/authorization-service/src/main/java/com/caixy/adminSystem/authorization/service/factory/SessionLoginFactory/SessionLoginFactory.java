package com.caixy.adminSystem.authorization.service.factory.SessionLoginFactory;

import com.caixy.adminSystem.common.api.user.vo.LoginUserVO;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.base.constant.UserConstant;
import com.caixy.adminSystem.common.base.constant.UserRoleEnum;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.utils.ServletUtils;
import com.caixy.adminSystem.authorization.service.factory.AuthorizationFactory;

import com.caixy.adminSystem.infrastucture.cache.redis.RedisManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Session登录服务类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Authorization.factory.SessionLoginFactory.SessionLoginService
 * @since 2024/10/27 01:39
 */
@Service
@ConditionalOnProperty(name = "login.type", havingValue = "SESSION", matchIfMissing = true)
@Slf4j
public class SessionLoginFactory implements AuthorizationFactory
{

    private final SessionRepository<? extends Session> sessionRepository;
    private final RedisManager redisManager;

    @Value("${login.singleLogin:false}")
    private boolean singleLogin;

    // Redis中存储所有活跃sessionId的集合key
    private static final String SESSION_ACTIVE_SET = "session:activeSessions";

    // Redis中存储用户与sessionId映射的key前缀
    private static final String SESSION_USER_KEY_PREFIX = "session:user:";

    public SessionLoginFactory(SessionRepository<? extends Session> sessionRepository,
                               RedisManager redisManager)
    {
        this.sessionRepository = sessionRepository;
        this.redisManager = redisManager;
    }

    @Override
    public String getName()
    {
        return "Session验证服务";
    }

    @Override
    public Boolean checkLogin(HttpServletRequest request)
    {
        return ServletUtils.getAttributeFromSessionOrNull(UserConstant.USER_LOGIN_STATE, UserVO.class, request.getSession()) != null;
    }

    @Override
    public Boolean checkLogin()
    {
        HttpServletRequest request = ServletUtils.getRequest();
        HttpSession session = request.getSession();
        return ServletUtils.getAttributeFromSessionOrNull(UserConstant.USER_LOGIN_STATE, UserVO.class, session) != null;
    }

    @Override
    public void checkLoginOrThrow()
    {
        if (!checkLogin())
        {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
    }

    @Override
    public Boolean asRole(UserRoleEnum roleEnum)
    {
        UserVO loginUser = getLoginUser();
        return loginUser.getUserRole().equals(roleEnum);
    }


    @Override
    public LoginUserVO doLogin(UserVO userVO, HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String sessionId = session.getId();

        setUserLoginInfo(userVO, request);
        // 处理单点登录
        if (singleLogin)
        {
            // 强制下线之前的登录
            forceLogout(userVO.getId());
        }

        // 将用户信息存入Session
        session.setAttribute(UserConstant.USER_LOGIN_STATE, userVO);

        // 将sessionId添加到活跃session集合
        redisManager.addToSet(SESSION_ACTIVE_SET, sessionId);

        // 将sessionId添加到用户的session集合
        String userSessionKey = getUserSessionKey(userVO.getId());
        redisManager.addToSet(userSessionKey, sessionId);

        return getLoginUserVO(userVO);
    }


    @Override
    public boolean doLogout()
    {
        HttpServletRequest request = ServletUtils.getRequest();
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        UserVO userVO = (UserVO) session.getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userVO != null)
        {
            Long userId = userVO.getId();
            // 从用户的session集合中移除
            String userSessionKey = getUserSessionKey(userId);
            redisManager.removeFromSet(userSessionKey, sessionId);
        }
        // 从活跃session集合中移除
        redisManager.removeFromSet(SESSION_ACTIVE_SET, sessionId);
        // 使Session失效
        session.invalidate();
        return true;
    }


    @Override
    public UserVO getLoginUser(HttpServletRequest request)
    {
        return ServletUtils.getAttributeFromSession(UserConstant.USER_LOGIN_STATE, UserVO.class, request.getSession())
                           .filter(user -> user.getId() != null)
                           .filter(user -> {
                               if (UserRoleEnum.BAN.equals(user.getUserRole())) {
                                   doLogout();
                                   throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被封禁");
                               }
                               return true;
                           })
                           .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGIN_ERROR));
    }

    @Override
    public UserVO getLoginUserPermitNull(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        return ServletUtils.getAttributeFromSession(UserConstant.USER_LOGIN_STATE, UserVO.class, session)
                           .filter(user ->
                           {
                               if (UserRoleEnum.BAN.equals(user.getUserRole()))
                               {
                                   doLogout();
                                   throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被封禁");
                               }
                               return true;
                           })
                           .orElse(null);
    }

    @Override
    public Boolean isAdmin()
    {
        return asRole(UserRoleEnum.ADMIN);
    }

    @Override
    public List<UserVO> getAllLoggedInUsers(int currentSize, int size)
    {
        Set<String> sessionIds = redisManager.getMembersFromSet(SESSION_ACTIVE_SET);
        List<UserVO> userList = new ArrayList<>();
        if (sessionIds != null && !sessionIds.isEmpty())
        {
            List<String> sessionIdList = new ArrayList<>(sessionIds);

            // 实现分页
            int totalSessions = sessionIdList.size();
            int fromIndex = (currentSize - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalSessions);
            if (fromIndex >= totalSessions)
            {
                return userList; // 返回空列表
            }
            List<String> pagedSessionIds = sessionIdList.subList(fromIndex, toIndex);

            for (String sessionId : pagedSessionIds)
            {
                Session session = sessionRepository.findById(sessionId);
                if (session != null)
                {
                    UserVO userVO = session.getAttribute(UserConstant.USER_LOGIN_STATE);
                    if (userVO != null)
                    {
                        userList.add(userVO);
                    }
                    else
                    {
                        // Session 不包含用户信息，可能已过期
                        // 从活跃session集合中移除无效的sessionId
                        redisManager.removeFromSet(SESSION_ACTIVE_SET, sessionId);
                    }
                }
                else
                {
                    // Session 为null，可能已过期
                    // 从活跃session集合中移除无效的sessionId
                    redisManager.removeFromSet(SESSION_ACTIVE_SET, sessionId);
                }
            }
        }
        return userList;
    }


    @Override
    public int getLoggedInUserCount()
    {
        Long size = redisManager.getSetSize(SESSION_ACTIVE_SET);
        return size != null ? size.intValue() : 0;
    }


    @Override
    public void forceLogout(Long userId)
    {
        String userSessionKey = getUserSessionKey(userId);
        Set<String> sessionIds = redisManager.getMembersFromSet(userSessionKey);
        if (sessionIds != null)
        {
            for (String sessionId : sessionIds)
            {
                // 删除Session
                sessionRepository.deleteById(sessionId);
                // 从活跃session集合中移除
                redisManager.removeFromSet(SESSION_ACTIVE_SET, sessionId);
            }
            // 删除用户的session集合
            redisManager.delete(userSessionKey);
        }
    }

    private String getUserSessionKey(Long userId)
    {
        return SESSION_USER_KEY_PREFIX + userId;
    }

}
