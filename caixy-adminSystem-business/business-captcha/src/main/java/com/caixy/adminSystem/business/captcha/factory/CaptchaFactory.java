package com.caixy.adminSystem.business.captcha.factory;

import cn.hutool.core.util.RandomUtil;
import com.caixy.adminSystem.business.captcha.annotation.CaptchaTypeTarget;
import com.caixy.adminSystem.business.captcha.strategy.CaptchaGenerationStrategy;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.common.base.utils.ServletUtils;
import com.caixy.adminSystem.common.base.utils.SpringContextUtils;
import com.caixy.adminSystem.infrastucture.cache.redis.RedisManager;
import com.caixy.adminSystem.infrastucture.cache.redis.domain.RedisKeyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码生成工厂
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.factory.CaptchaFactory
 * @since 2024-07-16 03:33
 **/
@Component
@Slf4j
public class CaptchaFactory
{
    @Resource
    private List<CaptchaGenerationStrategy> captchaGenerationStrategies;

    @Resource
    private RedisManager redisManager;

    private ConcurrentHashMap<String, CaptchaGenerationStrategy> serviceCache;

    private List<CaptchaGenerationStrategy> registeredStrategies;

    @PostConstruct
    public void initActionService()
    {
        serviceCache =
                SpringContextUtils.getServiceFromAnnotation(captchaGenerationStrategies, CaptchaTypeTarget.class, "value");
        registeredStrategies = new ArrayList<>(serviceCache.values());
    }

    public CaptchaGenerationStrategy getCaptchaStrategy(String type)
    {
        return serviceCache.get(type);
    }

    public CaptchaGenerationStrategy getRandomCaptchaStrategy()
    {
        return RandomUtil.randomEle(registeredStrategies);
    }

    public boolean verifyCaptcha(String captchaCode)
    {
        String sessionId = ServletUtils.getSessionId();

        // 获取存储在 Redis 中的验证码
        Map<String, Object> result = redisManager.getHashMap(RedisKeyEnum.CAPTCHA_CODE, String.class, String.class, sessionId);
        if (result == null || result.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        String redisCode = Optional.of(result.get("code"))
                                   .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期"))
                                   .toString().trim();

        // 校验验证码（不区分大小写），同时删除 Redis 中的验证码，确保验证码的单次有效性
        boolean isVerified = redisCode.equalsIgnoreCase(captchaCode.trim());
        if (isVerified)
        {
            redisManager.delete(RedisKeyEnum.CAPTCHA_CODE, sessionId);
        }
        else {
            log.warn("验证码校验失败，sessionId: {}", sessionId);
        }

        return isVerified;
    }
}
