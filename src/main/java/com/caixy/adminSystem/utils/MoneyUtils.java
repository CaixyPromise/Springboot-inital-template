package com.caixy.adminSystem.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.MoneyUtils
 * @since 2024/10/20 01:49
 */
public class MoneyUtils {

    /**
     * 元转分
     *
     * @param number
     * @return
     */
    public static Long yuanToCent(BigDecimal number)
    {
        return number.multiply(new BigDecimal("100")).longValue();
    }

    /**
     * 分转元
     *
     * @param number
     * @return
     */
    public static BigDecimal centToYuan(Long number)
    {
        if (number == null)
        {
            return null;
        }
        return new BigDecimal(number.toString()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}