package com.caixy.adminSystem.constant;

import java.util.regex.Pattern;

/**
 * @Name: com.caixy.adminSystem.constant.RegexPatternConstants
 * @Description: 正则匹配符
 * @Author: CAIXYPROMISE
 * @Date: 2024-04-26 19:49
 **/
public interface RegexPatternConstants
{
    /**
     * 匹配汉字
     */
    String CHINESE_REGEX = "^[\\u4e00-\\u9fa5]{0,}$";
    /**
     * 匹配英文和数字
     */
    String ENGLISH_NUMBER_REGEX = "^[A-Za-z0-9]+$";

    /**
     * 匹配大写英文字母
     */
    String UPPERCASE_ENGLISH_REGEX = "^[A-Z]+$";

    /**
     * 匹配小写英文字母
     */
    String LOWERCASE_ENGLISH_REGEX = "^[a-z]+$";

    /**
     * 匹配邮箱是否合法
     */
    String EMAIL_REGEX = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";

    /**
     * 邮箱加密， 邮箱第一个字符和’@‘之后的原文显示，第一个字符之后’@‘之前的，显示为’****’
     */
    String EMAIL_ENCRYPT_REGEX = "(^\\w)[^@]*(@.*$)";

    /**
     * 匹配手机号是否合法
     */
    String PHONE_REGEX = "^1[3-9]\\d{9}$";

    /**
     * 手机号加密，屏蔽 中间 4位数
     */
    String PHONE_ENCRYPT_REGEX = "(^\\d{3})\\d.*(\\d{4})";


    /**
     * 匹配URL
     */
    String URL_REGEX = "[a-zA-z]+://[^\\s]*";

    /**
     * 身份证匹配
     */
    String ID_CARD_REGEX = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)";


    /**
     * 匹配域名
     */
    String DOMAIN_REGEX = "[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?";


    /**
     * 匹配密码是否合法 (必须包含大小写字母和数字的组合，可以使用特殊字符，长度在8-20之间)
     */
    String PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20}$";
    
    /**
     * IPV4 匹配
     */
    String IPV4_REGEX = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";

    /**
     * 匹配账号：必须以字母开头，长度在5到16字节之间，且只允许包含字母和数字（不允许下划线
     */
    String ACCOUNT_REGEX = "^[a-zA-Z][a-zA-Z0-9]{4,15}$";

    /**
     * 提取JSON
     */
    Pattern EXTRA_JSON_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);

    /**
     * 匹配姓名，只能包含中文字符和英文大小写字母
     */
    String NAME_REGEX = "^(?:[\\u4e00-\\u9fa5a-zA-Z]{1,30})$";

}
