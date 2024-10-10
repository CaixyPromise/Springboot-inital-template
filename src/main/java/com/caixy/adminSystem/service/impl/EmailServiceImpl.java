package com.caixy.adminSystem.service.impl;


import cn.hutool.core.util.RandomUtil;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.exception.ThrowUtils;
import com.caixy.adminSystem.manager.Email.EmailSenderManager;
import com.caixy.adminSystem.manager.Email.core.EmailSenderDTO;
import com.caixy.adminSystem.manager.Email.core.EmailSenderEnum;
import com.caixy.adminSystem.model.dto.email.SendEmailRequest;
import com.caixy.adminSystem.service.EmailService;
import com.caixy.adminSystem.utils.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮箱服务类实现
 *
 * @name: com.caixy.adminSystem.service.impl.EmailServiceImpl
 * @author: CAIXYPROMISE
 * @since: 2024-01-10 22:01
 **/
@Service
@Slf4j
@AllArgsConstructor
public class EmailServiceImpl implements EmailService
{
    private final EmailSenderManager emailSenderManager;
    private final RedisUtils redisUtils;

    @Override
    public Boolean sendEmail(SendEmailRequest sendEmailRequest, EmailSenderEnum senderEnum, HttpServletRequest request)
    {
        // 根据发送类型进行不同的处理
        switch (senderEnum)
        {
            case FORGET_PASSWORD:
            case REGISTER:
            case RESET_PASSWORD:
            case RESET_EMAIL:
                return doSendCaptcha(sendEmailRequest, senderEnum, request);
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 发送验证码统一方法
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/10 下午7:11
     */
    private Boolean doSendCaptcha(SendEmailRequest sendEmailRequest, EmailSenderEnum senderEnum, HttpServletRequest request)
    {
        // 获取目标邮箱
        String toEmail = sendEmailRequest.getToEmail();
        // 检查目标邮箱是否为空
        ThrowUtils.throwIf(StringUtils.isBlank(toEmail), ErrorCode.PARAMS_ERROR);
        //检查是否发送
        checkHasSend(toEmail, senderEnum);
        // 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 配置邮件内容参数
        Map<String, Object> params = new HashMap<>();
        // 设置redis缓存信息，包括验证码，发送时间，发送的会话id
        params.put("code", code);
        params.put("send_time", String.valueOf(System.currentTimeMillis()));
        params.put("send_sessionId", request.getSession().getId());
        // 将验证码存入Redis，设置过期时间为5分钟
        redisUtils.setHashMap(senderEnum, params, toEmail);
        // 异步发送邮件时，上层调用不关心发送是否成功，已配置默认线程池失败策略为丢弃消息
        emailSenderManager.doSendBySync(senderEnum, new EmailSenderDTO(toEmail), params);
        return true;
    }


    /**
     * 检查是否发送
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/10 下午7:12
     */
    private void checkHasSend(String toEmail, EmailSenderEnum senderEnum)
    {
        // 检查目标邮箱
        ThrowUtils.throwIf(StringUtils.isBlank(toEmail), ErrorCode.PARAMS_ERROR, "邮箱不得为空");
        // 检查是否重复发送
        boolean hasSend = redisUtils.hasKey(senderEnum, toEmail);
        if (hasSend)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮件已发送，请到邮箱内查收。");
        }
    }
}
