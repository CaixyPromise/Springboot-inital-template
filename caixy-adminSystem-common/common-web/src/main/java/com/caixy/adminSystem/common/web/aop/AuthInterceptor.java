package com.caixy.adminSystem.common.web.aop;

import com.caixy.adminSystem.authorization.service.manager.AuthManager;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.base.constant.UserRoleEnum;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.web.annotation.AuthCheck;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验 AOP
 */
@Aspect
@Component
public class AuthInterceptor
{
    @Autowired
    private AuthManager authManager;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable
    {
        UserRoleEnum mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        UserVO loginUser = authManager.getLoginUser();
        UserRoleEnum userRole = loginUser.getUserRole();

        // 必须有该权限才通过
        if (mustRole == null || userRole == null)
        {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 如果被封号，直接拒绝
        if (UserRoleEnum.BAN.equals(mustRole))
        {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 必须有管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRole))
        {
            if (!mustRole.equals(userRole))
            {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

