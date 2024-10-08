package com.caixy.adminSystem.manager.Email.utils;

import com.caixy.adminSystem.manager.Email.constant.EmailConstant;
import com.caixy.adminSystem.manager.Email.core.BaseEmailContentDTO;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.StringWriter;

/**
 * FreeMark邮件模板工具类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.utils.FreeMarkEmailUtil
 * @since 2024/10/6 下午3:57
 */
public class FreeMarkEmailUtil
{
    private FreeMarkEmailUtil() {}

    private static final Configuration configuration;

    static
    {
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassForTemplateLoading(FreeMarkEmailUtil.class, EmailConstant.EMAIL_HTML_PATH);
        configuration.setDefaultEncoding(EmailConstant.EMAIL_HTML_ENCODING);
        configuration.setNumberFormat("0.######");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    /**
     * 根据模板路径和参数，生成模板内容
     *
     * @param templateName 模板名称 (e.g., "captcha.ftl")
     * @param baseEmailContentDTO  数据模型，基类 {@link BaseEmailContentDTO}
     * @return 渲染后的模板内容
     */
    public static String generateContent(@NotNull String templateName, @NotNull BaseEmailContentDTO baseEmailContentDTO)
    {
        try
        {
            Template template = configuration.getTemplate(templateName);
            StringWriter out = new StringWriter();
            template.process(baseEmailContentDTO, out);
            return out.toString();
        }
        catch (IOException | TemplateException e)
        {
            throw new RuntimeException("Error while processing FreeMarker template", e);
        }
    }

}
