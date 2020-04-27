package com.bajins.demo.time;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JDK1.7及以下使用此类
 * 需要包joda-time-2.9.9.jar
 *
 * @program: com.zd966.file.cloud.utils
 * @description: DataTimeUtil
 * @author:
 * @create: 2018-04-13 17:34
 */
public class Time7Util {

    //精确时间到纳秒
    //private static Long DAY = System.nanoTime();


    // 多线程开启
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>();

    /**
     * 获取格式化
     *
     * @param pattern
     * @return
     */
    public static SimpleDateFormat getSDF(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            pattern = TimeStyle.YYYY_MM_DD_HH_MM_SS.getValue();
        }
        SimpleDateFormat dateFormat = DATE_FORMAT_THREAD_LOCAL.get();
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
            dateFormat.setLenient(false);
            DATE_FORMAT_THREAD_LOCAL.set(dateFormat);
        }
        dateFormat.applyPattern(pattern);
        return dateFormat;
    }

    /**
     * 使用默认格式进行时间格式化
     *
     * @return java.lang.String
     */
    public static String getDateFormat() {
        SimpleDateFormat sdf = getSDF(null);
        String format = sdf.format(new Date());//格式化日期
        return format;
    }

    /**
     * 传入枚举类DateStyle中的值获取格式化后的字符串时间
     *
     * @param timeStyle
     * @return
     */
    public static String getDateFormatStyle(TimeStyle timeStyle) {
        SimpleDateFormat sdf = getSDF(timeStyle.getValue());
        String format = sdf.format(new Date());//格式化日期
        return format;
    }

    /**
     * 传入String类型时间格式化
     *
     * @param date String类型时间
     * @return java.lang.String
     */
    public static String getDateFormatString(String date) {
        String trim = date.trim();
        SimpleDateFormat sdf = getSDF(null);
        String format;
        if (!date.equals("") && date != null) {
            format = sdf.format(trim);//格式化日期
        } else {

            format = sdf.format(new Date());//格式化日期
        }
        return format;
    }

    /**
     * 传入Date类型时间格式化
     *
     * @param date 类型时间
     * @return java.lang.String
     */
    public static String getDateFormat(Date date) {
        SimpleDateFormat sdf = getSDF(null);
        String format;
        if (!date.equals("") && date != null) {
            format = sdf.format(date);//格式化日期
        } else {

            format = sdf.format(new Date());//格式化日期
        }
        return format;
    }

    /**
     * 传入Long类型时间格式化
     *
     * @param date Long类型时间
     * @return java.lang.String
     */
    public static String getDateFormat(Long date) {
        SimpleDateFormat sdf = getSDF(null);
        String format;
        if (!date.equals("") && date != null) {
            format = sdf.format(date);//格式化日期
        } else {
            format = sdf.format(new Date());//格式化日期
        }
        return format;
    }

    /**
     * 传入string类型时间和格式进行格式化
     *
     * @param date          Date类型的时间
     * @param specification 格式
     * @return java.lang.String
     */
    public static String getDateFormat(Date date, String specification) {
        if (specification.equals("")) {
            return null;
        }
        String sTrim = specification.trim();
        SimpleDateFormat df = new SimpleDateFormat(sTrim);//设置日期格式
        String format;
        if (!date.equals("") && date != null) {
            format = df.format(date);//格式化日期
        } else {
            format = df.format(new Date());//格式化日期
        }
        return format;
    }

    /**
     * 传入string类型时间和格式进行格式化
     *
     * @param date          string类型的时间
     * @param specification 格式
     * @return java.lang.String
     */
    public static String getDateFormat(String date, String specification) {
        if (specification.equals("")) {
            return null;
        }
        String sTrim = specification.trim();
        String trim = date.trim();
        SimpleDateFormat df = new SimpleDateFormat(sTrim);//设置日期格式
        String format;
        if (!date.equals("") && date != null) {
            format = df.format(trim);//格式化日期
        } else {

            format = df.format(new Date());//格式化日期
        }
        return format;
    }


    /**
     * 传入Long类型时间和格式进行格式化
     *
     * @param date          Long类型的时间
     * @param specification 格式
     * @return java.lang.String
     */
    public static String getDateFormat(Long date, String specification) {
        if (specification.equals("")) {
            return null;
        }
        String sTrim = specification.trim();
        SimpleDateFormat df = new SimpleDateFormat(sTrim);//设置日期格式
        String format;
        if (!date.equals("") && date != null) {
            format = df.format(date);//格式化日期
        } else {

            format = df.format(new Date());//格式化日期
        }
        return format;
    }

    /**
     * 获取时间加减
     *
     * @param year  传入大于0的数字相应的就减少多少年
     * @param month 传入大于0的数字相应的就减少多少月
     * @return Date
     */
    public static Date getTime(int year, int month) {
        Date date = new Date();
        Date calendarTime = getAddSubtractTime(date, year, month);
        return calendarTime;
    }

    /**
     * 获取时间加减
     *
     * @param date  传入String类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return Date
     */
    public static Date getTime(String date, int year, int month) throws ParseException {
        SimpleDateFormat sdf = getSDF(null);
        Date parse = sdf.parse(date);
        Date addSubtractTime = getAddSubtractTime(parse, year, month);
        return addSubtractTime;
    }

    /**
     * 获取时间加减
     *
     * @param date  传入Date类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return Calendar
     */
    public static Calendar getCalendar(Date date, int year, int month) {
        // 获取当前时间
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 加减年份
        cal.add(Calendar.YEAR, year);
        // 加减月份
        cal.add(Calendar.MONTH, month);
        return cal;
    }


    /**
     * 获取时间加减后的前月最后一天
     *
     * @param date  传入Date类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return
     */
    public static String getAddSubtractLastDayOfMonth(Date date, int year, int month) {
        Calendar calendar = getCalendar(date, year, month);
        return getLastDayOfMonth(calendar.getTime(), 0, 0);
    }

    /**
     * 获取时间加减后的前月第一天
     *
     * @param date  传入Date类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return
     */
    public static String getAddSubtractFirstDayOfMonth(Date date, int year, int month) {
        Calendar calendar = getCalendar(date, year, month);
        return getFirstDayOfMonth(calendar.getTime(), 0, 0);
    }

    /**
     * 获取时间加减后的本月最小天数
     *
     * @param date  传入Date类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return
     */
    public static int getAddSubtractMonthFirstDay(Date date, int year, int month) {
        Calendar calendar = getCalendar(date, year, month);
        return calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取时间加减后的本月最大天数
     *
     * @param date  传入Date类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return
     */
    public static int getAddSubtractMonthLastDay(Date date, int year, int month) {
        Calendar calendar = getCalendar(date, year, month);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取时间加减后的天数
     *
     * @param date  传入Date类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return
     */
    public static int getAddSubtractDay(Date date, int year, int month) {
        Calendar calendar = getCalendar(date, year, month);
        return calendar.get(Calendar.DATE);
    }

    /**
     * 获取时间加减后的时间
     *
     * @param date  传入Date类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return
     */
    public static Date getAddSubtractTime(Date date, int year, int month) {
        Calendar calendar = getCalendar(date, year, month);
        return calendar.getTime();
    }

    /**
     * 获取时间加减后的月份
     *
     * @param date  传入Date类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return
     */
    public static int getAddSubtractMonth(Date date, int year, int month) {
        Calendar calendar = getCalendar(date, year, month);
        // 获取月，因为第一个月是0，所以要+1
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取时间加减后的年份
     *
     * @param date  传入Date类型日期
     * @param year  传入大于0相应加多少年，小于0相应减多少年
     * @param month 传入大于0相应加多少月，小于0相应减多少月
     * @return
     */
    public static int getAddSubtractYear(Date date, int year, int month) {
        Calendar calendar = getCalendar(date, year, month);
        return calendar.get(Calendar.YEAR);
    }


    /**
     * 计算两个日期之间的天数
     *
     * @param start开始日期
     * @param end结束日期
     * @return int
     */
    public static int countTwoDate(Date start, Date end) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        long time1 = cal.getTimeInMillis();
        cal.setTime(end);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 获取某年天数
     *
     * @param year 年份
     * @return int
     */
    public static int getYearDays(int year) {
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {// 闰年的判断规则
            return 366;
        } else {
            return 365;
        }
    }

    /**
     * 获取某年天数
     *
     * @param datedate类型时间
     * @return int
     */
    public static int getYearDays(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);// 获取年
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {// 闰年的判断规则
            return 366;
        } else {
            return 365;
        }
    }

    /**
     * 获取某年天数
     *
     * @param date String类型时间
     * @return int
     */
    public static int getYearDays(String date) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = getSDF(null);
        cal.setTime(sdf.parse(date));
        int year = cal.get(Calendar.YEAR);// 获取年
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {// 闰年的判断规则
            return 366;
        } else {
            return 365;
        }
    }

    /**
     * 获取当前年天数
     *
     * @param
     * @return int
     */
    public static int getYearDays() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);// 获取年
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {// 闰年的判断规则
            return 366;
        } else {
            return 365;
        }
    }

    /**
     * 获得该月第一天
     *
     * @param year年份
     * @param month月份
     * @return java.lang.String
     */
    public static String getFirstDayOfMonth(Date date, int year, int month) {
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }
        // 设置年份
        //cal.set(Calendar.YEAR, year);
        // 加减年份
        cal.add(Calendar.YEAR, year);
        // 设置月份
        //cal.set(Calendar.MONTH, month - 1);
        // 加减月份
        cal.add(Calendar.MONTH, month);
        // 获取某月最小天数
        int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最小天数
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String firstDayOfMonth = sdf.format(cal.getTime());
        return firstDayOfMonth;
    }

    /**
     * 获得该月最后一天
     *
     * @param year 年份
     * @param month 月份
     * @return java.lang.String
     */
    public static String getLastDayOfMonth(Date date, int year, int month) {
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }
        // 设置年份
        //cal.set(Calendar.YEAR, year);
        // 加减年份
        cal.add(Calendar.YEAR, year);
        // 设置月份
        //cal.set(Calendar.MONTH, month - 1);
        // 加减月份
        cal.add(Calendar.MONTH, month);
        // 获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
        return lastDayOfMonth;
    }

    /**
     * 根据周数，获取开始日期、结束日期
     *
     * @param week 周期  0本周，-1上周，-2上上周，1下周，2下下周
     * @return 返回date[0]开始日期、date[1]结束日期
     */
    public static Date[] getWeekStartAndEnd(int week) {
        DateTime dateTime = new DateTime();
        LocalDate date = new LocalDate(dateTime.plusWeeks(week));

        date = date.dayOfWeek().withMinimumValue();
        Date beginDate = date.toDate();
        Date endDate = date.plusDays(6).toDate();
        return new Date[]{beginDate, endDate};
    }

    /**
     * 对日期的【秒】进行加/减
     *
     * @param date    日期
     * @param seconds 秒数，负数为减
     * @return 加/减几秒后的日期
     */
    public static Date addDateSeconds(Date date, int seconds) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusSeconds(seconds).toDate();
    }

    /**
     * 对日期的【分钟】进行加/减
     *
     * @param date    日期
     * @param minutes 分钟数，负数为减
     * @return 加/减几分钟后的日期
     */
    public static Date addDateMinutes(Date date, int minutes) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusMinutes(minutes).toDate();
    }

    /**
     * 对日期的【小时】进行加/减
     *
     * @param date  日期
     * @param hours 小时数，负数为减
     * @return 加/减几小时后的日期
     */
    public static Date addDateHours(Date date, int hours) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusHours(hours).toDate();
    }

    /**
     * 对日期的【天】进行加/减
     *
     * @param date 日期
     * @param days 天数，负数为减
     * @return 加/减几天后的日期
     */
    public static Date addDateDays(Date date, int days) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusDays(days).toDate();
    }

    /**
     * 对日期的【周】进行加/减
     *
     * @param date  日期
     * @param weeks 周数，负数为减
     * @return 加/减几周后的日期
     */
    public static Date addDateWeeks(Date date, int weeks) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusWeeks(weeks).toDate();
    }

    /**
     * 对日期的【月】进行加/减
     *
     * @param date   日期
     * @param months 月数，负数为减
     * @return 加/减几月后的日期
     */
    public static Date addDateMonths(Date date, int months) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusMonths(months).toDate();
    }

    /**
     * 对日期的【年】进行加/减
     *
     * @param date  日期
     * @param years 年数，负数为减
     * @return 加/减几年后的日期
     */
    public static Date addDateYears(Date date, int years) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusYears(years).toDate();
    }


    /**
     * 获取日期中的某数值。如获取月份
     *
     * @param date     日期
     * @param dateType 日期格式
     * @return 数值
     */
    private static int getInteger(Date date, int dateType) {
        int num = 0;
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
            num = calendar.get(dateType);
        }
        return num;
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     *
     * @param date     日期字符串
     * @param dateType 类型
     * @param amount   数值
     * @return 计算后日期字符串
     */
    private static String addInteger(String date, int dateType, int amount) throws ParseException {
        String dateString = null;
        TimeStyle timeStyle = getDateStyle(date);
        if (timeStyle != null) {
            Date myDate = StringToDate(date, timeStyle);
            myDate = addInteger(myDate, dateType, amount);
            dateString = DateToString(myDate, timeStyle);
        }
        return dateString;
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     *
     * @param date     日期
     * @param dateType 类型
     * @param amount   数值
     * @return 计算后日期
     */
    private static Date addInteger(Date date, int dateType, int amount) {
        Date myDate = null;
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(dateType, amount);
            myDate = calendar.getTime();
        }
        return myDate;
    }

    /**
     * 获取精确的日期
     *
     * @param timestamps 时间long集合
     * @return 日期
     */
    private static Date getAccurateDate(List<Long> timestamps) {
        Date date = null;
        long timestamp = 0;
        Map<Long, long[]> map = new HashMap<Long, long[]>();
        List<Long> absoluteValues = new ArrayList<Long>();

        if (timestamps != null && timestamps.size() > 0) {
            if (timestamps.size() > 1) {
                for (int i = 0; i < timestamps.size(); i++) {
                    for (int j = i + 1; j < timestamps.size(); j++) {
                        long absoluteValue = Math.abs(timestamps.get(i) - timestamps.get(j));
                        absoluteValues.add(absoluteValue);
                        long[] timestampTmp = {timestamps.get(i), timestamps.get(j)};
                        map.put(absoluteValue, timestampTmp);
                    }
                }

                // 有可能有相等的情况。如2012-11和2012-11-01。时间戳是相等的。此时minAbsoluteValue为0
                // 因此不能将minAbsoluteValue取默认值0
                long minAbsoluteValue = -1;
                if (!absoluteValues.isEmpty()) {
                    minAbsoluteValue = absoluteValues.get(0);
                    for (int i = 1; i < absoluteValues.size(); i++) {
                        if (minAbsoluteValue > absoluteValues.get(i)) {
                            minAbsoluteValue = absoluteValues.get(i);
                        }
                    }
                }

                if (minAbsoluteValue != -1) {
                    long[] timestampsLastTmp = map.get(minAbsoluteValue);

                    long dateOne = timestampsLastTmp[0];
                    long dateTwo = timestampsLastTmp[1];
                    if (absoluteValues.size() > 1) {
                        timestamp = Math.abs(dateOne) > Math.abs(dateTwo) ? dateOne : dateTwo;
                    }
                }
            } else {
                timestamp = timestamps.get(0);
            }
        }

        if (timestamp != 0) {
            date = new Date(timestamp);
        }
        return date;
    }

    /**
     * 判断字符串是否为日期字符串
     *
     * @param date 日期字符串
     * @return true or false
     */
    public static boolean isDate(String date) {
        boolean isDate = false;
        if (date != null) {
            if (getDateStyle(date) != null) {
                isDate = true;
            }
        }
        return isDate;
    }

    /**
     * 获取日期字符串的日期风格。失敗返回null。
     *
     * @param date 日期字符串
     * @return 日期风格
     */
    public static TimeStyle getDateStyle(String date) {
        TimeStyle timeStyle = null;
        Map<Long, TimeStyle> map = new HashMap<Long, TimeStyle>();
        List<Long> timestamps = new ArrayList<Long>();
        for (TimeStyle style : TimeStyle.values()) {
            if (style.isShowOnly()) {
                continue;
            }
            Date dateTmp = null;
            if (date != null) {
                ParsePosition pos = new ParsePosition(0);
                dateTmp = getSDF(style.getValue()).parse(date, pos);
                if (pos.getIndex() != date.length()) {
                    dateTmp = null;
                }
            }
            if (dateTmp != null) {
                timestamps.add(dateTmp.getTime());
                map.put(dateTmp.getTime(), style);
            }
        }
        Date accurateDate = getAccurateDate(timestamps);
        if (accurateDate != null) {
            timeStyle = map.get(accurateDate.getTime());
        }
        return timeStyle;
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date 日期字符串
     * @return 日期
     */
    public static Date StringToDate(String date) throws ParseException {
        TimeStyle timeStyle = getDateStyle(date);
        return StringToDate(date, timeStyle);
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date    日期字符串
     * @param pattern 日期格式
     * @return 日期
     */
    public static Date StringToDate(String date, String pattern) throws ParseException {
        Date myDate = null;
        if (date != null) {
            myDate = getSDF(pattern).parse(date);
        }
        return myDate;
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date      日期字符串
     * @param timeStyle 日期风格
     * @return 日期
     */
    public static Date StringToDate(String date, TimeStyle timeStyle) throws ParseException {
        Date myDate = null;
        if (timeStyle != null) {
            myDate = StringToDate(date, timeStyle.getValue());
        }
        return myDate;
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     *
     * @param date    日期
     * @param pattern 日期格式
     * @return 日期字符串
     */
    public static String DateToString(Date date, String pattern) {
        String dateString = null;
        if (date != null) {
            dateString = getSDF(pattern).format(date);
        }
        return dateString;
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     *
     * @param date      日期
     * @param timeStyle 日期风格
     * @return 日期字符串
     */
    public static String DateToString(Date date, TimeStyle timeStyle) {
        String dateString = null;
        if (timeStyle != null) {
            dateString = DateToString(date, timeStyle.getValue());
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date       旧日期字符串
     * @param newPattern 新日期格式
     * @return 新日期字符串
     */
    public static String StringToString(String date, String newPattern) throws ParseException {
        TimeStyle oldTimeStyle = getDateStyle(date);
        return StringToString(date, oldTimeStyle, newPattern);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param newTimeStyle 新日期风格
     * @return 新日期字符串
     */
    public static String StringToString(String date, TimeStyle newTimeStyle) throws ParseException {
        TimeStyle oldTimeStyle = getDateStyle(date);
        return StringToString(date, oldTimeStyle, newTimeStyle);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date       旧日期字符串
     * @param oldPattern 旧日期格式
     * @param newPattern 新日期格式
     * @return 新日期字符串
     */
    public static String StringToString(String date, String oldPattern, String newPattern) throws ParseException {
        return DateToString(StringToDate(date, oldPattern), newPattern);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date        旧日期字符串
     * @param oldDteStyle 旧日期风格
     * @param newParttern 新日期格式
     * @return 新日期字符串
     */
    public static String StringToString(String date, TimeStyle oldDteStyle, String newParttern) throws ParseException {
        String dateString = null;
        if (oldDteStyle != null) {
            dateString = StringToString(date, oldDteStyle.getValue(), newParttern);
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param oldPattern   旧日期格式
     * @param newTimeStyle 新日期风格
     * @return 新日期字符串
     */
    public static String StringToString(String date, String oldPattern, TimeStyle newTimeStyle) throws ParseException {
        String dateString = null;
        if (newTimeStyle != null) {
            dateString = StringToString(date, oldPattern, newTimeStyle.getValue());
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param oldDteStyle  旧日期风格
     * @param newTimeStyle 新日期风格
     * @return 新日期字符串
     */
    public static String StringToString(String date, TimeStyle oldDteStyle, TimeStyle newTimeStyle) throws ParseException {
        String dateString = null;
        if (oldDteStyle != null && newTimeStyle != null) {
            dateString = StringToString(date, oldDteStyle.getValue(), newTimeStyle.getValue());
        }
        return dateString;
    }

    /**
     * 增加日期的年份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加年份后的日期字符串
     */
    public static String addYear(String date, int yearAmount) throws ParseException {
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的年份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加年份后的日期
     */
    public static Date addYear(Date date, int yearAmount) {
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     *
     * @param date        日期
     * @param monthAmount 增加数量。可为负数
     * @return 增加月份后的日期字符串
     */
    public static String addMonth(String date, int monthAmount) throws ParseException {
        return addInteger(date, Calendar.MONTH, monthAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     *
     * @param date        日期
     * @param monthAmount 增加数量。可为负数
     * @return 增加月份后的日期
     */
    public static Date addMonth(Date date, int monthAmount) {
        return addInteger(date, Calendar.MONTH, monthAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     *
     * @param date      日期字符串
     * @param dayAmount 增加数量。可为负数
     * @return 增加天数后的日期字符串
     */
    public static String addDay(String date, int dayAmount) throws ParseException {
        return addInteger(date, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     *
     * @param date      日期
     * @param dayAmount 增加数量。可为负数
     * @return 增加天数后的日期
     */
    public static Date addDay(Date date, int dayAmount) {
        return addInteger(date, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     *
     * @param date       日期字符串
     * @param hourAmount 增加数量。可为负数
     * @return 增加小时后的日期字符串
     */
    public static String addHour(String date, int hourAmount) throws ParseException {
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     *
     * @param date       日期
     * @param hourAmount 增加数量。可为负数
     * @return 增加小时后的日期
     */
    public static Date addHour(Date date, int hourAmount) {
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     *
     * @param date         日期字符串
     * @param minuteAmount 增加数量。可为负数
     * @return 增加分钟后的日期字符串
     */
    public static String addMinute(String date, int minuteAmount) throws ParseException {
        return addInteger(date, Calendar.MINUTE, minuteAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     *
     * @param date         日期
     * @param minuteAmount 增加数量。可为负数
     * @return 增加分钟后的日期
     */
    public static Date addMinute(Date date, int minuteAmount) {
        return addInteger(date, Calendar.MINUTE, minuteAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     *
     * @param date         日期字符串
     * @param secondAmount 增加数量。可为负数
     * @return 增加秒钟后的日期字符串
     */
    public static String addSecond(String date, int secondAmount) throws ParseException {
        return addInteger(date, Calendar.SECOND, secondAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     *
     * @param date         日期
     * @param secondAmount 增加数量。可为负数
     * @return 增加秒钟后的日期
     */
    public static Date addSecond(Date date, int secondAmount) {
        return addInteger(date, Calendar.SECOND, secondAmount);
    }

    /**
     * 获取日期的年份。失败返回0。
     *
     * @param date 日期字符串
     * @return 年份
     */
    public static int getYear(String date) throws ParseException {
        return getYear(StringToDate(date));
    }

    /**
     * 获取日期的年份。失败返回0。
     *
     * @param date 日期
     * @return 年份
     */
    public static int getYear(Date date) {
        return getInteger(date, Calendar.YEAR);
    }

    /**
     * 获取日期的月份。失败返回0。
     *
     * @param date 日期字符串
     * @return 月份
     */
    public static int getMonth(String date) throws ParseException {
        return getMonth(StringToDate(date));
    }

    /**
     * 获取日期的月份。失败返回0。
     *
     * @param date 日期
     * @return 月份
     */
    public static int getMonth(Date date) {
        return getInteger(date, Calendar.MONTH) + 1;
    }

    /**
     * 获取日期的天数。失败返回0。
     *
     * @param date 日期字符串
     * @return 天
     */
    public static int getDay(String date) throws ParseException {
        return getDay(StringToDate(date));
    }

    /**
     * 获取日期的天数。失败返回0。
     *
     * @param date 日期
     * @return 天
     */
    public static int getDay(Date date) {
        return getInteger(date, Calendar.DATE);
    }

    /**
     * 获取日期的小时。失败返回0。
     *
     * @param date 日期字符串
     * @return 小时
     */
    public static int getHour(String date) throws ParseException {
        return getHour(StringToDate(date));
    }

    /**
     * 获取日期的小时。失败返回0。
     *
     * @param date 日期
     * @return 小时
     */
    public static int getHour(Date date) {
        return getInteger(date, Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取日期的分钟。失败返回0。
     *
     * @param date 日期字符串
     * @return 分钟
     */
    public static int getMinute(String date) throws ParseException {
        return getMinute(StringToDate(date));
    }

    /**
     * 获取日期的分钟。失败返回0。
     *
     * @param date 日期
     * @return 分钟
     */
    public static int getMinute(Date date) {
        return getInteger(date, Calendar.MINUTE);
    }

    /**
     * 获取日期的秒钟。失败返回0。
     *
     * @param date 日期字符串
     * @return 秒钟
     */
    public static int getSecond(String date) throws ParseException {
        return getSecond(StringToDate(date));
    }

    /**
     * 获取日期的秒钟。失败返回0。
     *
     * @param date 日期
     * @return 秒钟
     */
    public static int getSecond(Date date) {
        return getInteger(date, Calendar.SECOND);
    }

    /**
     * 获取日期 。默认yyyy-MM-dd格式。失败返回null。
     *
     * @param date 日期字符串
     * @return 日期
     */
    public static String getDate(String date) throws ParseException {
        return StringToString(date, TimeStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期。默认yyyy-MM-dd格式。失败返回null。
     *
     * @param date 日期
     * @return 日期
     */
    public static String getDate(Date date) {
        return DateToString(date, TimeStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     *
     * @param date 日期字符串
     * @return 时间
     */
    public static String getTime(String date) throws ParseException {
        return StringToString(date, TimeStyle.HH_MM_SS);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     *
     * @param date 日期
     * @return 时间
     */
    public static String getTime(Date date) {
        return DateToString(date, TimeStyle.HH_MM_SS);
    }

    /**
     * 获取日期的星期。失败返回null。
     *
     * @param date 日期字符串
     * @return 星期
     */
    public static Week getWeek(String date) throws ParseException {
        Week week = null;
        TimeStyle timeStyle = getDateStyle(date);
        if (timeStyle != null) {
            Date myDate = StringToDate(date, timeStyle);
            week = getWeek(myDate);
        }
        return week;
    }

    /**
     * 获取日期的星期。失败返回null。
     *
     * @param date 日期
     * @return 星期
     */
    public static Week getWeek(Date date) {
        Week week = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekNumber = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        switch (weekNumber) {
            case 0:
                week = Week.SUNDAY;
                break;
            case 1:
                week = Week.MONDAY;
                break;
            case 2:
                week = Week.TUESDAY;
                break;
            case 3:
                week = Week.WEDNESDAY;
                break;
            case 4:
                week = Week.THURSDAY;
                break;
            case 5:
                week = Week.FRIDAY;
                break;
            case 6:
                week = Week.SATURDAY;
                break;
        }
        return week;
    }

    /**
     * 获取两个日期相差的天数
     *
     * @param date      日期字符串
     * @param otherDate 另一个日期字符串
     * @return 相差天数。如果失败则返回-1
     */
    public static int getIntervalDays(String date, String otherDate) throws ParseException {
        return getIntervalDays(StringToDate(date), StringToDate(otherDate));
    }

    /**
     * @param date      日期
     * @param otherDate 另一个日期
     * @return 相差天数。如果失败则返回-1
     */
    public static int getIntervalDays(Date date, Date otherDate) throws ParseException {
        int num = -1;
        Date dateTmp = StringToDate(getDate(date), TimeStyle.YYYY_MM_DD);
        Date otherDateTmp = StringToDate(getDate(otherDate), TimeStyle.YYYY_MM_DD);
        if (dateTmp != null && otherDateTmp != null) {
            long time = Math.abs(dateTmp.getTime() - otherDateTmp.getTime());
            num = (int) (time / (24 * 60 * 60 * 1000));
        }
        return num;
    }

    /**
     * 获取两个日期相差的天数，后一个日期的天数减去前一个日期的天数。也就是说，如果后者时间大于前者，则返回正值，否则返回负值。
     *
     * @param date      日期
     * @param otherDate 另一个日期
     * @return 相差天数。如果失败则返回-1
     */
    public static int calculateIntervalDays(Date date, Date otherDate) throws ParseException {
        int num = -1;
        Date dateTmp = StringToDate(getDate(date), TimeStyle.YYYY_MM_DD);
        Date otherDateTmp = StringToDate(getDate(otherDate), TimeStyle.YYYY_MM_DD);
        if (dateTmp != null && otherDateTmp != null) {
            long time = otherDateTmp.getTime() - dateTmp.getTime();
            num = (int) (time / (24 * 60 * 60 * 1000));
        }
        return num;
    }

    public static int getIntervalDaysToToday(Date date) throws ParseException {
        return getIntervalDays(date, new Date());
    }

    /**
     * 获取简单农历对象
     *
     * @param date 日期字符串
     * @return 简单农历对象
     */
    public static LunarCalendar getSimpleLunarCalendar(String date) throws ParseException {
        return new LunarCalendar(StringToDate(date));
    }

    /**
     * 获取简单农历对象
     *
     * @param date 日期
     * @return 简单农历对象
     */
    public static LunarCalendar getSimpleLunarCalendar(Date date) {
        return new LunarCalendar(date);
    }

    /**
     * 判断日期是否在当前的月份
     *
     * @return
     */
    public static boolean isInCurrentMonth(Date date) {
        if (getMonth(new Date()) - getMonth(date) == 0) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前年
     *
     * @return
     */
    public static int getCurrentYear() {
        return getYear(new Date());
    }

    /**
     * 获取date中当月的最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfMonth(Date date) throws ParseException {
        return addDay(addMonth(StringToDate(DateToString(date, TimeStyle.YYYY_MM_CN)), 1), -1);
    }

    /**
     * 获取某天零时
     *
     * @param date
     * @return
     */
    public static Date getZero(Date date) throws ParseException {
        return StringToDate(DateToString(date, TimeStyle.YYYY_MM_DD), TimeStyle.YYYY_MM_DD);
    }

    /**
     * 距离现在的时间，返回类似"3个月前"的字段
     */
    public static String getHowLong(Date date) {
        long now = new Date().getTime();
        long s = 1000l;
        long m = s * 60;
        long h = m * 60;
        long d = h * 24;
        long M = d * 30;
        long y = d * 365;
        long howLong = now - date.getTime();
        if (howLong >= y) {
            return (howLong / y) + "年前";
        } else if (howLong >= M) {
            return (howLong / M) + "个月前";
        } else if (howLong > d) {
            return (howLong / d) + "天前";
        } else if (howLong > h) {
            return (howLong / h) + "小时前";
        } else if (howLong > m) {
            return (howLong / m) + "分钟前";
        } else {
            return (howLong / s) + "秒前";
        }
    }

    public static int getCurrentMonthLastDay(Date date) {
        Calendar a = Calendar.getInstance();
        a.setTime(date);
        //        a.set(Calendar.DATE, 1);//把日期设置为当月第一天
        //        a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天
        int maxDate = a.getActualMaximum(Calendar.DAY_OF_MONTH);
        return maxDate;
    }

    /**
     * 获取距离今天多长分钟
     *
     * @param date
     * @return
     */
    public static long getMinuteTONow(Date date) {
        Calendar a = Calendar.getInstance();
        return getMinuteBetween(date, a.getTime());
    }

    /**
     * 计算两个日期之间的分钟数
     *
     * @param date
     * @param otherDate 大的
     * @return
     */
    public static long getMinuteBetween(Date date, Date otherDate) {
        long min = (otherDate.getTime() - date.getTime()) / 60000;
        return min;
    }
}


