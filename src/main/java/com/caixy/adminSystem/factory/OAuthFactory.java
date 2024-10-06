package com.caixy.adminSystem.factory;

import com.caixy.adminSystem.manager.OAuth.annotation.OAuthTypeTarget;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.dto.oauth.OAuthResultResponse;
import com.caixy.adminSystem.model.enums.OAuthProviderEnum;
import com.caixy.adminSystem.strategy.OAuth2ActionStrategy;
import com.caixy.adminSystem.utils.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth服务工厂
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.factory.OAuthFactory
 * @since 2024/8/3 下午4:00
 */
@Component
public class OAuthFactory
{
    private static final Logger log = LoggerFactory.getLogger(OAuthFactory.class);
    @Resource
    private List<OAuth2ActionStrategy<?, ?, ?, ?>> oAuth2ActionStrategy;

    private ConcurrentHashMap<OAuthProviderEnum, OAuth2ActionStrategy<?, ?, ?, ?>> oAuth2ActionStrategyMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init()
    {
        oAuth2ActionStrategyMap = SpringContextUtils.getServiceFromAnnotation(oAuth2ActionStrategy,
                OAuthTypeTarget.class, "clientName");
        log.info("OAuth2ActionStrategyMap: {}", oAuth2ActionStrategyMap);
    }

    @SuppressWarnings("unchecked")
    public <CPRT, UPT, GAURT, GCRT> OAuth2ActionStrategy<CPRT, UPT, GAURT, GCRT>
    getOAuth2ActionStrategy(OAuthProviderEnum providerEnum)
    {
        OAuth2ActionStrategy<CPRT, UPT, GAURT, GCRT> strategyService = (OAuth2ActionStrategy<CPRT, UPT, GAURT, GCRT>) oAuth2ActionStrategyMap.get(
                providerEnum);
        if (strategyService == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的OAuth2登录方式");
        }
        return strategyService;
    }

    public OAuthResultResponse doAuth(OAuthProviderEnum providerEnum, Map<String, Object> paramMaps)
    {
        return getOAuth2ActionStrategy(providerEnum).doAuth(paramMaps);
    }


}
