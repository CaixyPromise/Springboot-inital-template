package com.caixy.adminSystem.strategy;

import cn.hutool.core.codec.Base64;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.constant.CommonConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.enums.RedisConstant;
import com.caixy.adminSystem.model.vo.captcha.CaptchaVO;
import com.caixy.adminSystem.utils.RedisUtils;
import com.google.code.kaptcha.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FastByteArrayOutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 验证码生成抽象类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.strategy.CaptchaGenerationStrategy
 * @since 2024-07-15 19:43
 **/
public abstract class CaptchaGenerationStrategy
{
    private static final Logger log = LoggerFactory.getLogger(CaptchaGenerationStrategy.class);

    protected Producer captchaProducer;

    @PostConstruct
    public void init()
    {
        captchaProducer = makeProducer();
    }

    @Resource
    private RedisUtils redisUtils;

    public abstract CaptchaVO generateCaptcha(HttpServletRequest request);

    protected abstract Producer makeProducer(); // 确保这个方法只能被继承者使用

    public boolean verifyCaptcha(String captchaCode, HttpServletRequest request)
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
        return !redisCode.equalsIgnoreCase(captchaCode.trim()) && sessionUuid.equals(redisUuid);
    }

    protected CaptchaVO saveResult(String code, BufferedImage image, HttpServletRequest request)
    {
        HashMap<String, Object> resultMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString();

        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "jpg", outputStream);
        }
        catch (IOException e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        resultMap.put("uuid", uuid);
        resultMap.put("code", code);
        // 写入redis
        // 以uuid作为凭证，
        // 并设置过期时间: 5分钟
        redisUtils.setHashMap(RedisConstant.CAPTCHA_CODE,
                resultMap,
                request.getRequestedSessionId());
        // 过期时间5分钟
        // 返回Base64的验证码图片信息
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCodeImage(Base64.encode(outputStream.toByteArray()));
        captchaVO.setUuid(uuid);
        request.getSession().setAttribute(CommonConstant.CAPTCHA_SIGN, uuid);
        return captchaVO;
    }

    protected void tryRemoveLastCaptcha(HttpServletRequest request)
    {
        Object lastUuid = request.getSession().getAttribute(CommonConstant.CAPTCHA_SIGN);
        if (lastUuid != null)
        {
            redisUtils.delete(RedisConstant.CAPTCHA_CODE, request.getRequestedSessionId());
        }
    }
}
