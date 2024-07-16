package com.caixy.adminSystem.factory;

import cn.hutool.core.util.RandomUtil;
import com.caixy.adminSystem.annotation.CaptchaTypeTarget;
import com.caixy.adminSystem.strategy.CaptchaGenerationStrategy;
import com.caixy.adminSystem.utils.SpringContextUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码生成工厂
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.factory.CaptchaFactory
 * @since 2024-07-16 03:33
 **/
@Component
public class CaptchaFactory
{
    private ConcurrentHashMap<String, CaptchaGenerationStrategy> serviceCache;

    @Resource
    private List<CaptchaGenerationStrategy> captchaGenerationStrategies;

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
}
