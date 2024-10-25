package com.caixy.adminSystem.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 算术工具类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.ArithmeticUtils
 * @since 2024/10/20 01:50
 */
public class ArithmeticUtils
{
    /** 默认除法运算精度 */
    private static final int DEF_DIV_SCALE = 10;

    /** 这个类不能实例化 */
    private ArithmeticUtils()
    {
    }

    // 加法
    @NotNull
    @Contract(pure = true)
    public static BigDecimal add(@NotNull BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    // 减法
    @NotNull
    @Contract(pure = true)
    public static BigDecimal subtract(@NotNull BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    // 乘法
    @NotNull
    @Contract(pure = true)
    public static BigDecimal multiply(@NotNull BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }

    // 除法
    @NotNull
    @Contract(pure = true)
    public static BigDecimal divide(@NotNull BigDecimal a, BigDecimal b, int scale, RoundingMode roundingMode) {
        return a.divide(b, scale, roundingMode);
    }

    // 四舍五入
    @NotNull
    @Contract(pure = true)
    public static BigDecimal round(@NotNull BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 提供精确的加法运算。
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static double add(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算。
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static double sub(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算。
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static double mul(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后10位，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1, double v2)
    {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        if (b1.compareTo(BigDecimal.ZERO) == 0)
        {
            return BigDecimal.ZERO.doubleValue();
        }
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入处理。
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = BigDecimal.ONE;
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }
}
