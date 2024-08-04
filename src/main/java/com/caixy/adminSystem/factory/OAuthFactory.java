package com.caixy.adminSystem.factory;

import com.caixy.adminSystem.annotation.OAuthTypeTarget;
import com.caixy.adminSystem.strategy.OAuth2ActionStrategy;
import com.caixy.adminSystem.utils.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth服务工厂
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.factory.OAuthFactory
 * @since 2024/8/3 下午4:00
 */
@Component
public class OAuthFactory<
        CallbackPayloadResponseType, // 回调参数类型
        UserProfileType,    // 用户信息类型
        GetAuthorizationUrlRequestType, // 获取授权URL类型
        GetCallbackRequestType>
{
    private static final Logger log = LoggerFactory.getLogger(OAuthFactory.class);
    @Resource
    private List<OAuth2ActionStrategy<
            CallbackPayloadResponseType,
            UserProfileType,
            GetAuthorizationUrlRequestType,
            GetCallbackRequestType>> oAuth2ActionStrategy;

    private ConcurrentHashMap<String, OAuth2ActionStrategy<CallbackPayloadResponseType, UserProfileType, GetAuthorizationUrlRequestType, GetCallbackRequestType>> oAuth2ActionStrategyMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init()
    {
        oAuth2ActionStrategyMap = SpringContextUtils.getServiceFromAnnotation(oAuth2ActionStrategy,
                OAuthTypeTarget.class, "clientName");
        log.info("OAuth2ActionStrategyMap: {}", oAuth2ActionStrategyMap);
    }

    public OAuth2ActionStrategy<CallbackPayloadResponseType, UserProfileType, GetAuthorizationUrlRequestType, GetCallbackRequestType> getOAuth2ActionStrategy(String clientName) {
        return oAuth2ActionStrategyMap.get(clientName);
    }

}
