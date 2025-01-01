package com.caixy.adminSystem.business.captcha.strategy;

import cn.hutool.core.codec.Base64;

import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.captcha.domain.CaptchaVO;
import com.caixy.adminSystem.common.base.utils.ServletUtils;
import com.caixy.adminSystem.infrastucture.cache.redis.RedisManager;
import com.caixy.adminSystem.infrastucture.cache.redis.domain.RedisKeyEnum;
import com.google.code.kaptcha.Producer;
import org.springframework.util.FastByteArrayOutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

/**
 * 验证码生成抽象类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.strategy.CaptchaGenerationStrategy
 * @since 2024-07-15 19:43
 **/
public abstract class CaptchaGenerationStrategy
{
    @Resource
    private RedisManager redisManager;

    protected Producer captchaProducer;

    @PostConstruct
    public void init()
    {
        captchaProducer = makeProducer();
    }

    public abstract CaptchaVO generateCaptcha(HttpServletRequest request);

    protected abstract Producer makeProducer(); // 确保这个方法只能被继承者使用

    protected CaptchaVO saveResult(String code, BufferedImage image, HttpServletRequest request)
    {
        HashMap<String, String> resultMap = new HashMap<>();

        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "jpg", outputStream);
        }
        catch (IOException e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        resultMap.put("code", code);
        // 写入redis
        // 以uuid作为凭证，
        // 并设置过期时间: 5分钟
        redisManager.setHashMap(RedisKeyEnum.CAPTCHA_CODE,
                resultMap,
                ServletUtils.getSession().getId());
        // 过期时间5分钟
        // 返回Base64的验证码图片信息
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCodeImage(Base64.encode(outputStream.toByteArray()));
        return captchaVO;
    }

    protected void tryRemoveLastCaptcha(HttpServletRequest request)
    {
        redisManager.delete(RedisKeyEnum.CAPTCHA_CODE, ServletUtils.getSession().getId());
    }
}
