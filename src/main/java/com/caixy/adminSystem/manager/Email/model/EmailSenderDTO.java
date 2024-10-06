package com.caixy.adminSystem.manager.Email.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送邮件的实体类，发送邮件内容需要继承此类。
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.core.model.EmailSenderDTO
 * @since 2024/10/1 下午3:46
 */
@Data
public class EmailSenderDTO implements Serializable
{
    /**
     * 目标邮箱
     */
    private String toEmail;

    /**
     * 邮件主题
     */
    private String subject;
    private static final long serialVersionUID = 1L;
}
