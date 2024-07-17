package com.caixy.adminSystem.controller;

import com.caixy.adminSystem.annotation.RateLimitFlow;
import com.caixy.adminSystem.common.BaseResponse;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.common.ResultUtils;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.enums.RedisLimiterEnum;
import com.caixy.adminSystem.model.vo.captcha.CaptchaVO;
import com.caixy.adminSystem.service.CaptchaService;
import com.caixy.adminSystem.utils.AesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * 验证码接口控制器
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.controller.CaptchaController
 * @since 2024-07-16 03:59
 **/
@Slf4j
@RestController
@RequestMapping("/captcha")
public class CaptchaController
{
    @Resource
    private CaptchaService captchaService;

    @GetMapping("/get")
    @RateLimitFlow(key = RedisLimiterEnum.CAPTCHA, args = "#request.getSession().getId()")
    public BaseResponse<CaptchaVO> getCaptcha(HttpServletRequest request)
    {
        return ResultUtils.success(captchaService.getAnyCaptcha(request));
    }
}
