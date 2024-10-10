package com.caixy.adminSystem.manager.Email.core;

import com.caixy.adminSystem.manager.Email.constant.EmailConstant;
import lombok.Data;
import lombok.Getter;
import org.joda.time.LocalDateTime;

import java.io.Serializable;

/**
 * 基础Email邮件模板FreeMark基类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.core.BaseFreeMarkDTO
 * @since 2024/10/6 下午4:01
 */
@Getter
public class BaseEmailContentDTO implements Serializable
{
    /**
     * 平台联系方式
     */
    private final String platformContact = EmailConstant.PLATFORM_CONTACT;

    /**
     * 平台负责人
     */
    private final String platformResponsiblePerson = EmailConstant.PLATFORM_RESPONSIBLE_PERSON;

    /**
     * 平台名称
     */
    private final String platformName = EmailConstant.PLATFORM_NAME_CN;

    /**
     * 平台英文名
     */
    private final String platformEnName = EmailConstant.PLATFORM_NAME_EN;

    /**
     * 当前年份
     */
    private final Integer currentYear = new LocalDateTime().getYear();
    private static final long serialVersionUID = 1L;
}
