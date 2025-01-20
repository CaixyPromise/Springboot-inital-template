package com.caixy.adminSystem.common.web.aop;

import com.caixy.adminSystem.authorization.service.manager.AuthManager;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.web.constant.WebConstant;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录信息拦截器
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/20 1:37
 */
@Component
@RequiredArgsConstructor
public class LoginUserInfoInterceptor implements HandlerInterceptor
{
    private final AuthManager authManager;
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        UserVO user = authManager.getLoginUser();
        if (user != null) {
            request.setAttribute(WebConstant.LOGIN_USER_INFO_KEY, user);
        }
        return true;
    }
    @Override
    public void afterCompletion(@NotNull HttpServletRequest request,@NotNull HttpServletResponse response,@NotNull Object handler, Exception ex) {
        request.removeAttribute(WebConstant.LOGIN_USER_INFO_KEY);
    }
}
