package com.caixy.adminSystem.manager.captcha;

import com.caixy.adminSystem.annotation.CaptchaTypeTarget;
import com.caixy.adminSystem.model.vo.captcha.CaptchaVO;
import com.caixy.adminSystem.strategy.CaptchaGenerationStrategy;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.util.Properties;

import static com.google.code.kaptcha.Constants.*;

/**
 * 默认验证码生成
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.captcha.DefaultCaptchaStrategyImpl
 * @since 2024-07-16 02:53
 **/
@Component
@CaptchaTypeTarget("Default")
public class DefaultCaptchaStrategyImpl extends CaptchaGenerationStrategy
{
    @Override
    public CaptchaVO generateCaptcha(HttpServletRequest request)
    {
        tryRemoveLastCaptcha(request);
        String capStr = null, code = null;

        capStr = code = captchaProducer.createText();
        BufferedImage image = captchaProducer.createImage(capStr);

        return saveResult(code, image, request);
    }

    @Override
    protected Producer makeProducer()
    {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 是否有边框 默认为true 我们可以自己设置yes，no
        properties.setProperty(KAPTCHA_BORDER, "no");
        // 验证码文本字符颜色 默认为Color.BLACK
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_COLOR, "black");
        // 验证码图片宽度 默认为200
        properties.setProperty(KAPTCHA_IMAGE_WIDTH, "170");
        // 验证码图片高度 默认为50
        properties.setProperty(KAPTCHA_IMAGE_HEIGHT, "60");
        // 验证码文本字符大小 默认为40
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_SIZE, "38");
        // KAPTCHA_SESSION_KEY
        properties.setProperty(KAPTCHA_SESSION_CONFIG_KEY, "kaptchaCode");
        // 验证码文本字符长度 默认为5
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        // 验证码文本字体样式 默认为new Font("Arial", 1, fontSize), new Font("Courier", 1, fontSize)
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Courier");
        // 图片样式 水纹com.google.code.kaptcha.impl.WaterRipple 鱼眼com.google.code.kaptcha.impl.FishEyeGimpy 阴影com.google.code.kaptcha.impl.ShadowGimpy
        properties.setProperty(KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.ShadowGimpy");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
