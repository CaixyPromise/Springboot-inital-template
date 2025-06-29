package com.caixy.adminSystem.common.email.utils;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * 邮箱验证码生成工具类
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/30 1:26
 */
public class EmailCodeGenerateUtils
{
    /**
     * 生成数字验证码
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/1/30 1:27
     */
    public static String generateNumber(int length) {
        return RandomStringUtils.randomNumeric(length);
    }
    /**
     * 生成字符串验证码
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/1/30 1:27
     */
    public static String generateString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }
    /**
     * 生成数字+字母验证码
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/1/30 1:27
     */
    public static String generateMixed(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
}
