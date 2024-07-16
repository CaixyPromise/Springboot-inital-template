package com.caixy.adminSystem.service.impl;

import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.factory.CaptchaFactory;
import com.caixy.adminSystem.model.vo.captcha.CaptchaVO;
import com.caixy.adminSystem.service.CaptchaService;
import com.caixy.adminSystem.strategy.CaptchaGenerationStrategy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    // todo: 添加验证码混淆，补充AES令牌进入到验证码图片中进行混淆。
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
}
