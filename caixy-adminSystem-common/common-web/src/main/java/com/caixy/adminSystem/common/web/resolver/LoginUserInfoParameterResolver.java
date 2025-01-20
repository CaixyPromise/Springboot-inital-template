package com.caixy.adminSystem.common.web.resolver;

import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.web.constant.WebConstant;
import com.caixy.adminSystem.common.web.resolver.annotation.LoginUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;


/**
 * 获取登录用户参数处理器
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/20 1:23
 */
@Component
public class LoginUserInfoParameterResolver implements HandlerMethodArgumentResolver
{
    @Override
    public boolean supportsParameter(@NotNull MethodParameter parameter)
    {
        return parameter.getParameterType().equals(UserVO.class)
               && parameter.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception
    {
        HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
        // 尝试从请求属性中获取用户信息
        UserVO user = (UserVO) request.getAttribute(WebConstant.LOGIN_USER_INFO_KEY);

        // 获取注解上的 required 属性
        LoginUser loginUserAnnotation = parameter.getParameterAnnotation(LoginUser.class);
        if (loginUserAnnotation != null && loginUserAnnotation.required() && user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录或登录已过期");
        }

        return user;
    }
}
