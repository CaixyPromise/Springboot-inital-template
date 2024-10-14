package com.caixy.adminSystem.controller;

import com.caixy.adminSystem.common.BaseResponse;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.common.ResultUtils;
import com.caixy.adminSystem.exception.ThrowUtils;
import com.caixy.adminSystem.manager.Email.core.EmailSenderEnum;
import com.caixy.adminSystem.model.dto.email.SendEmailRequest;
import com.caixy.adminSystem.model.vo.user.UserVO;
import com.caixy.adminSystem.service.EmailService;
import com.caixy.adminSystem.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Email发送接口控制器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.controller.EmailController
 * @since 2024/10/10 下午4:31
 */
@Slf4j
@RestController
@RequestMapping("/email")
@AllArgsConstructor
public class EmailController
{
    private final EmailService emailService;

    private final UserService userService;

    /**
     * 发送邮件信息
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/10 下午5:49
     */
    @PostMapping("/send")
    public BaseResponse<Boolean> sendEmail(@RequestBody SendEmailRequest sendEmailRequest, HttpServletRequest request)
    {
        // 无需校验邮箱
        Integer scenes = sendEmailRequest.getScenes();
        EmailSenderEnum senderEnum = EmailSenderEnum.getByCode(scenes);
        ThrowUtils.throwIf(senderEnum == null, ErrorCode.PARAMS_ERROR);
        UserVO userInfo = null;
        if (senderEnum.getRequireLogin())
        {
            userInfo = userService.getLoginUser(request);
        }
        if (senderEnum.getRequireToEmail())
        {
            ThrowUtils.throwIf(StringUtils.isBlank(sendEmailRequest.getToEmail()), ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(emailService.sendEmail(sendEmailRequest, senderEnum, request, userInfo));
    }
}
