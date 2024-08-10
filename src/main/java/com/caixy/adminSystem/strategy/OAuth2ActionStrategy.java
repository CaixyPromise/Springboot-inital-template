package com.caixy.adminSystem.strategy;

import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.dto.oauth.OAuthResultResponse;
import com.caixy.adminSystem.utils.RedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Resource;
import java.util.Map;

/**
 * OAuth2验证类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.strategy.OAuth2ActionStrategy
 * @since 2024/8/2 下午3:09
 */
public abstract class OAuth2ActionStrategy<
        CallbackPayloadResponseType, // 回调参数类型
        UserProfileType,    // 用户信息类型
        GetAuthorizationUrlRequestType, // 获取授权URL类型
        GetCallbackRequestType>    // 获取回调请求类型
{
    @Resource
    protected RedisUtils redisUtils;

    public abstract String getAuthorizationUrl(GetAuthorizationUrlRequestType authorizationUrlType);

    public abstract CallbackPayloadResponseType doCallback(GetCallbackRequestType callbackType);

    public abstract UserProfileType getUserProfile(CallbackPayloadResponseType callback);

    public abstract OAuthResultResponse doAuth(Map<String, Object> paramMaps);

    protected <T> T safetyConvertMapToObject(Map<String, Object> paramMaps, Class<T> clazz)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        T convertValue = objectMapper.convertValue(paramMaps, clazz);
        if (convertValue == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "授权失败");
        }
        return convertValue;
    }
}