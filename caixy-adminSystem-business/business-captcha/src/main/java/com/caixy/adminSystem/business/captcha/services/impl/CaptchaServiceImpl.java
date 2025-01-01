package com.caixy.adminSystem.business.captcha.services.impl;

import com.caixy.adminSystem.common.captcha.domain.CaptchaVO;
import com.caixy.adminSystem.common.captcha.service.CaptchaService;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.business.captcha.factory.CaptchaFactory;
import com.caixy.adminSystem.business.captcha.strategy.CaptchaGenerationStrategy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 验证码服务类接口实现类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.service.impl.CaptchaServiceImpl
 * @since 2024-07-16 04:09
 **/
@Service
@AllArgsConstructor
public class CaptchaServiceImpl implements CaptchaService
{
    private final CaptchaFactory captchaFactory;

    @Override
    public CaptchaVO getAnyCaptcha(HttpServletRequest request)
    {
        CaptchaGenerationStrategy randomCaptchaStrategy = captchaFactory.getRandomCaptchaStrategy();
        return randomCaptchaStrategy.generateCaptcha(request);
    }

    @Override
    public CaptchaVO getCaptchaByType(HttpServletRequest request, String type)
    {
        CaptchaGenerationStrategy captchaStrategy = captchaFactory.getCaptchaStrategy(type);
        if (captchaStrategy == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码类型错误");
        }
        return captchaStrategy.generateCaptcha(request);
    }

    /**
     * 校验验证码
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/7/18 上午1:54
     */
    @Override
    public boolean verifyCaptcha(String code)
    {
        return captchaFactory.verifyCaptcha(code);
    }
}
