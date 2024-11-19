package com.caixy.adminSystem.factory;

import cn.hutool.core.util.RandomUtil;
import com.caixy.adminSystem.manager.Captcha.annotation.CaptchaTypeTarget;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.constant.CommonConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.enums.RedisKeyEnum;
import com.caixy.adminSystem.strategy.CaptchaGenerationStrategy;
import com.caixy.adminSystem.utils.RedisUtils;
import com.caixy.adminSystem.utils.ServletUtils;
import com.caixy.adminSystem.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    private RedisUtils redisUtils;

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
        Map<String, String> result = redisUtils.getHashMap(RedisKeyEnum.CAPTCHA_CODE, String.class, String.class, sessionId);
        if (result == null || result.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        String redisCode = result.get("code").trim();

        // 校验验证码（不区分大小写），同时删除 Redis 中的验证码，确保验证码的单次有效性
        boolean isVerified = redisCode.equalsIgnoreCase(captchaCode.trim());
        if (isVerified)
        {
            redisUtils.delete(RedisKeyEnum.CAPTCHA_CODE, sessionId);
        }
        else {
            log.warn("验证码校验失败，sessionId: {}", sessionId);
        }

        return isVerified;
    }
}
