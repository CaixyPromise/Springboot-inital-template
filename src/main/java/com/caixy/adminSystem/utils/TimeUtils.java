package com.caixy.adminSystem.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 时间工具类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.TimeUtils
 * @since 2024/10/26 01:00
 */
public class TimeUtils
{
    /**
     * 获取时间单位对应的毫秒数
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/26 上午1:02
     */
    public static Long getMills(int quantity, TimeUnit timeUnit)
    {
        return timeUnit.toMillis(quantity);
    }
}
