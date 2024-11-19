package com.caixy.adminSystem.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

/**
 * 日期工具类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.DateUtils
 * @since 2024/10/20 02:08
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils
{
    // 设置默认时区为东八区
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate()
    {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate()
    {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime()
    {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow()
    {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format)
    {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date)
    {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final Date date)
    {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts)
    {
        try
        {
            return new SimpleDateFormat(format).parse(ts);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str)
    {
        if (str == null)
        {
            return null;
        }
        try
        {
            return parseDate(str.toString(), parsePatterns);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate()
    {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2)
    {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算时间差
     *
     * @param endDate   最后时间
     * @param startTime 开始时间
     * @return 时间差（天/小时/分钟）
     */
    public static String timeDistance(Date endDate, Date startTime)
    {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startTime.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    public static Date calcTimeDistance(Date endDate, Date startTime)
    {
        if (endDate == null || startTime == null)
        {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        // 计算时间差（毫秒）
        long diffInMillis = Math.abs(endDate.getTime() - startTime.getTime());

        // 将时间差转换为一个 Date 对象
        return new Date(diffInMillis);
    }

    /**
     * 将 LocalDateTime 转换为 Date
     *
     * @param localDateTime 要转换的 LocalDateTime
     * @return 转换后的 Date
     */
    public static Date toDate(LocalDateTime localDateTime)
    {
        return toDate(localDateTime, DEFAULT_ZONE);
    }

    /**
     * 增加 LocalDate ==> Date
     */
    public static Date toDate(LocalDate temporalAccessor)
    {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        return toDate(localDateTime);
    }


    /**
     * 判断目标时间是否早于参考时间
     *
     * @param targetDate    目标时间
     * @param referenceDate 参考时间
     * @return 如果目标时间早于参考时间，返回 true；否则返回 false
     */
    public static boolean isBefore(Date targetDate, Date referenceDate)
    {
        if (targetDate == null || referenceDate == null)
        {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        return targetDate.before(referenceDate);
    }

    /**
     * 判断目标时间是否晚于参考时间
     *
     * @param targetDate    目标时间
     * @param referenceDate 参考时间
     * @return 如果目标时间晚于参考时间，返回 true；否则返回 false
     */
    public static boolean isAfter(Date targetDate, Date referenceDate)
    {
        if (targetDate == null || referenceDate == null)
        {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        return targetDate.after(referenceDate);
    }

    /**
     * 判断目标时间是否晚于当前时间
     *
     * @param targetDate 目标时间
     * @return 如果目标时间晚于当前时间，返回 true；否则返回 false
     */
    public static boolean isAfterNow(Date targetDate)
    {
        if (targetDate == null)
        {
            throw new IllegalArgumentException("Target date cannot be null");
        }
        Date now = new Date();
        return targetDate.after(now);
    }

    /**
     * 判断目标时间是否早于当前时间
     *
     * @param targetDate 目标时间
     * @return 如果目标时间早于当前时间，返回 true；否则返回 false
     */
    public static boolean isBeforeNow(Date targetDate)
    {
        if (targetDate == null)
        {
            throw new IllegalArgumentException("Target date cannot be null");
        }
        Date now = new Date();
        return targetDate.before(now);
    }

    /**
     * 判断目标时间是否处于指定时间区间内（包含边界）
     *
     * @param targetDate 目标时间
     * @param startDate  区间开始时间
     * @param endDate    区间结束时间
     * @return 如果目标时间处于区间内，返回 true；否则返回 false
     */
    public static boolean isWithinRange(Date targetDate, Date startDate, Date endDate)
    {
        if (targetDate == null || startDate == null || endDate == null)
        {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        return !targetDate.before(startDate) && !targetDate.after(endDate);
    }


    /**
     * 将 LocalDateTime 转换为 Date，允许外部传入时区
     *
     * @param localDateTime 要转换的 LocalDateTime
     * @param zoneId        时区
     * @return 转换后的 Date
     */
    public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId)
    {
        if (localDateTime == null)
        {
            throw new IllegalArgumentException("LocalDateTime cannot be null");
        }
        if (zoneId == null)
        {
            throw new IllegalArgumentException("ZoneId cannot be null");
        }
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

    /**
     * 将 Date 转换为 LocalDateTime
     *
     * @param date 要转换的 Date
     * @return 转换后的 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date)
    {
        return toLocalDateTime(date, DEFAULT_ZONE);
    }

    /**
     * 将 Date 转换为 LocalDateTime，允许外部传入时区
     *
     * @param date   要转换的 Date
     * @param zoneId 时区
     * @return 转换后的 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId)
    {
        if (date == null)
        {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (zoneId == null)
        {
            throw new IllegalArgumentException("ZoneId cannot be null");
        }
        return date.toInstant().atZone(zoneId).toLocalDateTime();
    }
}
