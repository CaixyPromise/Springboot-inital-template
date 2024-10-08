package com.caixy.adminSystem.manager.Email.constant;

import com.caixy.adminSystem.manager.Email.utils.FreeMarkEmailUtil;

/**
 * 邮件配置常量
 * 因为{@link FreeMarkEmailUtil}需要进行静态初始化，所以使用接口类配置属性，而非springboot的配置类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.constant.EmailConstant
 * @since 2024/10/6 下午5:52
 */
public interface EmailConstant
{
    /**
     * 邮件html内容路径 resources目录
     */
    String EMAIL_HTML_PATH = "/email-template";

    /**
     * 邮件html内容编码
     */
    String EMAIL_HTML_ENCODING = "UTF-8";

    /**
     * 邮件平台名称-中文
     */
    String PLATFORM_NAME_CN = "CAIXYPROMISE Spring Boot快速启动后台模板";
    /**
     * 邮件平台名称-英文
     */
    String PLATFORM_NAME_EN = "CAIXYPROMISE Spring Boot Quick Start Backstage Template";
    /**
     * 邮件平台负责人
     */
    String PLATFORM_RESPONSIBLE_PERSON = "CAIXYPROMISE";

    /**
     * 邮件平台联系方式
     */
    String PLATFORM_CONTACT = "caixypromised@gmail.com";

}
