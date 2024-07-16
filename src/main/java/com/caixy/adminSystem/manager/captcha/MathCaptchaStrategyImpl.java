package com.caixy.adminSystem.manager.captcha;

import com.caixy.adminSystem.annotation.CaptchaTypeTarget;
import com.caixy.adminSystem.model.vo.captcha.CaptchaVO;
import com.caixy.adminSystem.strategy.CaptchaGenerationStrategy;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.text.impl.DefaultTextCreator;
import com.google.code.kaptcha.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.util.Properties;
import java.util.Random;

import static com.google.code.kaptcha.Constants.*;

/**
 * 数学验证码策略实现
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.captcha.MathCaptchaStrategyImpl
 * @since 2024-07-16 02:56
 **/
@Component
@CaptchaTypeTarget("Math")
public class MathCaptchaStrategyImpl extends CaptchaGenerationStrategy
{
    @Override
    public CaptchaVO generateCaptcha(HttpServletRequest request)
    {
        tryRemoveLastCaptcha(request);
        String capText = captchaProducer.createText();
        String capStr = capText.substring(0, capText.lastIndexOf("@"));
        System.out.println(capStr);
        String code = capText.substring(capText.lastIndexOf("@") + 1);
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
        // 边框颜色 默认为Color.BLACK
        properties.setProperty(KAPTCHA_BORDER_COLOR, "105,179,90");
        // 验证码文本字符颜色 默认为Color.BLACK
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_COLOR, "blue");
        // 验证码图片宽度 默认为200
        properties.setProperty(KAPTCHA_IMAGE_WIDTH, "160");
        // 验证码图片高度 默认为50
        properties.setProperty(KAPTCHA_IMAGE_HEIGHT, "60");
        // 验证码文本字符大小 默认为40
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_SIZE, "35");
        // KAPTCHA_SESSION_KEY
        properties.setProperty(KAPTCHA_SESSION_CONFIG_KEY, "kaptchaCodeMath");
        // 验证码文本生成器
        properties.setProperty(KAPTCHA_TEXTPRODUCER_IMPL, KaptchaMathTextCreator.class.getName());
        // 验证码文本字符间距 默认为2
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "3");
        // 验证码文本字符长度 默认为5
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "6");
        // 验证码文本字体样式 默认为new Font("Arial", 1, fontSize), new Font("Courier", 1, fontSize)
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Courier");
        // 验证码噪点颜色 默认为Color.BLACK
        properties.setProperty(KAPTCHA_NOISE_COLOR, "white");
        // 干扰实现类
        properties.setProperty(KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.NoNoise");
        // 图片样式 水纹com.google.code.kaptcha.impl.WaterRipple 鱼眼com.google.code.kaptcha.impl.FishEyeGimpy 阴影com.google.code.kaptcha.impl.ShadowGimpy
        properties.setProperty(KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.ShadowGimpy");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

    public static class KaptchaMathTextCreator extends DefaultTextCreator
    {
        private static final Logger log = LoggerFactory.getLogger(KaptchaMathTextCreator.class);
        private static final Random random = new Random();

        private static final String[] SYMBOL = {"+", "×", "-"};

        @Override
        public String getText()
        {
            int num1 = random.nextInt(10);
            int num2 = random.nextInt(10);
            String symbol = SYMBOL[random.nextInt(3)];

            return getResult(num1, num2, symbol.charAt(0));
        }

        private String getResult(int num1, int num2, char symbol)
        {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(num1)
                    .append(symbol)
                    .append(num2)
                    .append("=?@");
            int result = 0;
            switch (symbol)
            {
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            case '×':
                result = num1 * num2;
                break;
            case '÷':
                if (num1 % num2 != 0)
                {
                    num1 = num2 * random.nextInt(10);
                }
                result = num1 / num2;
                break;
            default:
                break;
            }

            stringBuilder.append(result);
            return stringBuilder.toString();
        }


//        @Override
//        public String getText() {
//            int result = 0;
//            int x = random.nextInt(10);
//            int y = random.nextInt(10);
//            StringBuilder suChinese = new StringBuilder();
//            int randomoperands = (int) Math.round(Math.random() * 2);
//            if (randomoperands == 0) {
//                result = x * y;
//                suChinese.append(CNUMBERS[x]);
//                suChinese.append("×");
//                suChinese.append(CNUMBERS[y]);
//            }
//            else if (randomoperands == 1){
//                if (!(x == 0) && y % x == 0){
//                    result = y / x;
//                    suChinese.append(CNUMBERS[y]);
//                    suChinese.append("÷");
//                    suChinese.append(CNUMBERS[x]);
//                } else {
//                    result = x + y;
//                    suChinese.append(CNUMBERS[x]);
//                    suChinese.append("+");
//                    suChinese.append(CNUMBERS[y]);
//                }
//            } else if (randomoperands == 2) {
//                if (x >= y) {
//                    result = x - y;
//                    suChinese.append(CNUMBERS[x]);
//                    suChinese.append("-");
//                    suChinese.append(CNUMBERS[y]);
//                } else {
//                    result = y - x;
//                    suChinese.append(CNUMBERS[y]);
//                    suChinese.append("-");
//                    suChinese.append(CNUMBERS[x]);
//                }
//            } else {
//                result = x + y;
//                suChinese.append(CNUMBERS[x]);
//                suChinese.append("+");
//                suChinese.append(CNUMBERS[y]);
//            }
//            suChinese.append("=?@").append(result);
//            return suChinese.toString();
//        }

    }
}



