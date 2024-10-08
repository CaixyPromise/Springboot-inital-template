package com.caixy.adminSystem.manager.Email.core;

import com.caixy.adminSystem.manager.Email.constant.EmailConstant;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 发送邮件的实体类，发送邮件内容需要继承此类。
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.core.EmailSenderDTO
 * @since 2024/10/1 下午3:46
 */
@Getter
@NoArgsConstructor
public class EmailSenderDTO implements Serializable
{
    /**
     * 目标邮箱
     */
    @Setter
    private String toEmail;

    /**
     * 邮件主题
     */
    private String subject;

    public EmailSenderDTO(String toEmail)
    {
        this.toEmail = toEmail;
    }


    /**
     * 构建邮件主题
     *
     * @param emailSenderEnum 邮件枚举
     * @return 邮件主题
     */
    public String buildSubjectTitle(EmailSenderEnum emailSenderEnum)
    {
        return baseSubjectTitle + emailSenderEnum.getName();
    }

    private static final String baseSubjectTitle = String.format("【%s】-", EmailConstant.PLATFORM_NAME_CN);

    private static final long serialVersionUID = 1L;
}
