package com.caixy.adminSystem.factory;

import cn.hutool.core.util.RandomUtil;
import com.caixy.adminSystem.annotation.CaptchaTypeTarget;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.constant.CommonConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.enums.RedisConstant;
import com.caixy.adminSystem.strategy.CaptchaGenerationStrategy;
import com.caixy.adminSystem.utils.RedisUtils;
import com.caixy.adminSystem.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public boolean verifyCaptcha(String captchaCode, String captchaId, HttpServletRequest request)
    {
        // 获取SessionId
        String sessionId = request.getRequestedSessionId();
        String sessionUuid = request.getSession().getAttribute(CommonConstant.CAPTCHA_SIGN).toString();
        // 1.2 校验验证码
        Map<String, String> result = redisUtils.getHashMap(
                RedisConstant.CAPTCHA_CODE,
                String.class,
                String.class,
                sessionId);
        if (sessionUuid == null || result == null || result.isEmpty())
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        String redisCode = result.get("code").trim();
        String redisUuid = result.get("uuid").trim();
        // 移除session缓存的uuid
        request.getSession().removeAttribute(CommonConstant.CAPTCHA_SIGN);
        boolean removeByCache = redisUtils.delete(RedisConstant.CAPTCHA_CODE, sessionId);
        if (!removeByCache)
        {
            log.warn("验证码校验失败，移除缓存失败，sessionId:{}", sessionId);
        }
        // 验证码不区分大小写，同时校验前后的session内的uuid是否一致。
        return !redisCode.equalsIgnoreCase(captchaCode.trim()) && sessionUuid.equals(captchaId) && captchaId.equals(redisUuid);
    }
}
