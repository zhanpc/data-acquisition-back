package com.maplestone.dataCollect.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class DateUtils {

    /**
     * 日期转字符串
     * 
     * @param date
     * @return
     */
    public static String dateToString(Date date, String formatStr) {
        String strTime = "";
        try {
            DateFormat df = new SimpleDateFormat(formatStr);
            strTime = df.format(date);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return strTime;
    }

    public static String dateToString(Date date) {
        return dateToString(date, "yyyy-MM-dd");
    }

    /**
     * 日期转字符串
     * 20170723121117
     * 
     * @param date
     * @return
     */
    public static String dateString(Date date) {
        return dateToString(date, "yyyyMMddHHmmss");
    }

    public static String convertString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 字符串转换到时间格式
     *
     * @param dateStr   需要转换的字符串
     * @param formatStr 需要格式的目标字符串 举例 yyyy-MM-dd
     * @return Date 返回转换后的时间
     * @throws ParseException 转换异常
     */
    public static Date stringToDate(String dateStr, String formatStr) {
        DateFormat sdf = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return date;
    }

    public static Date stringToDate(String dateStr) {
        return stringToDate(dateStr, "yyyy-MM-dd");
    }

    /**
     * 日期转字符串
     * 20170723121117
     * 
     * @param date
     * @return
     */
    public static String dateStringZH(Date date) {
        return dateToString(date, "yyyy年MM月dd日HH点mm分ss秒");
    }

    /**
     * 6 * 时间戳转换成日期格式字符串
     * 7 * @param seconds 精确到毫秒的字符串
     * 8 * @param formatStr
     * 9 * @return
     * 10
     */
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "")));
    }

    public static String shortTimeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }

    /**
     * 取得系统时间
     * 
     * @return
     */
    public static String getSystemTime(String format) {
        String strTime = "";
        DateFormat df = new SimpleDateFormat(format);
        strTime = df.format(new Date());
        return strTime;
    }

    public static String getSystemTime() {
        return getSystemTime("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 取得系统日期
     *
     * @return
     */
    public static String getSystemDate() {
        String strDate = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        strDate = df.format(new Date());
        return strDate;
    }

    /**
     * 取得系统时间
     */
    public static String getShortSystemTime() {
        return getSystemTime("yyyyMMddHHmmss");
    }

    /**
     * 取得系统时间2
     */
    public static String getShortSystemTime2() {
        return getSystemTime("yyMMddHHmmss");
    }

    /* UTC时间转换为标准时间 */
    public static Date getUTCtoGMT(int utc) {
        Integer integer = utc;
        if (integer.toString().length() < 10) {
            return null;
        }
        return new Date(Long.valueOf(utc) * 1000);

    }

    /**
     * 取得系统短日期，yymmdd
     */
    public static String getShortSystemDate() {
        String strTime;
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        strTime = df.format(new Date());
        return strTime;
    }

    /**
     * 判断两个日期差几天
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        return day + "";
    }

    /**
     * 系统时间加减
     *
     * @param date
     * @param dayNum
     * @return
     */
    public static String getOpeDate(String date, int dayNum) {
        Date dt = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dt = df.parse(date);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        GregorianCalendar gc = new GregorianCalendar();
        assert dt != null;
        gc.setTime(dt);
        gc.add(5, dayNum);
        // gc.set(gc.YEAR,gc.get(gc.MONTH),gc.get(gc.DATE));
        return String.valueOf(df.format(gc.getTime()));
    }

    /**
     * 系统时间加减
     */
    public static String getOpeMonth(String date, int monthNum) {
        Date dt = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dt = df.parse(date);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        GregorianCalendar gc = new GregorianCalendar();
        assert dt != null;
        gc.setTime(dt);
        gc.add(Calendar.MONTH, monthNum);
        // gc.set(gc.YEAR,gc.get(gc.MONTH),gc.get(gc.DATE));
        return String.valueOf(df.format(gc.getTime()));
    }

    /**
     * 取得两个日期的天数差 date2 - date1
     */
    public static long getDateDifference(String date1, String date2) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        ParsePosition pos1 = new ParsePosition(0);
        Date dt1 = formatter.parse(date1, pos);
        Date dt2 = formatter.parse(date2, pos1);
        return (dt2.getTime() - dt1.getTime()) / (3600 * 24 * 1000);
    }

    public static long getDateDifference(Date date1, Date date2) {
        return (date1.getTime() - date2.getTime()) / (3600 * 24 * 1000);
    }

    /**
     * 取得两个日期的时间差
     */
    public static long getHourDifference(Date date1, Date date2) {
        return (date1.getTime() - date2.getTime()) / (3600 * 1000);
    }

    /**
     * 取得两个日期的分钟差
     */
    public static long getMinDifference(Date date1, Date date2) {
        return (date1.getTime() - date2.getTime()) / (1000 * 60);
    }

    /**
     * 取得两个日期的秒差
     */
    public static long getSecDifference(Date date1, Date date2) {
        return (date1.getTime() - date2.getTime()) / (1000);
    }

    /**
     * 判断两个时间区间是否有交集的方法
     *  
     *
     * @param date1_1            区间1的时间始
     * @param date1_2            区间1的时间止
     * @param date2_1            区间2的时间始
     * @param date2_2            区间2的时间止
     * @return 区间1和区间2如果存在交集, 则返回true, 否则返回falses 时间临界点相等 不认为有交集
     */
    public static boolean isDateCross(Date date1_1, Date date1_2, Date date2_1, Date date2_2) {
        boolean flag = false;// 默认无交集
        long l1_1 = date1_1.getTime();
        long l1_2 = date1_2.getTime();
        long l2_1 = date2_1.getTime();
        long l2_2 = date2_2.getTime();

        if (((l1_1 <= l2_1) && (l2_2 <= l1_2)) || ((l2_1 <= l1_1) && (l1_1 < l2_2))
                || ((l2_1 < l1_2) && (l1_2 <= l2_2))) {
            flag = true;
        }
        return flag;
    }

    /**
     * 取得月份差
     */
    public static int getMonthDifference(String date1, String date2) {
        int result = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c1.setTime(sdf.parse(date1));
            c2.setTime(sdf.parse(date2));
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        result = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
        return result == 0 ? 1 : Math.abs(result);

    }

    /* 取得当月最后一天 */
    public static String getLastDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));// 年
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));// 月，因为Calendar里的月是从0开始，所以要减1
        cal.set(Calendar.DATE, 1);// 日，设为一号
        cal.add(Calendar.MONTH, 1);// 月份加一，得到下个月的一号
        cal.add(Calendar.DATE, -1);// 下一个月减一为本月最后一天
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());// 获得月末是几号
    }

    /* 取得当月第一天 */
    public static String getFirstDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));// 年
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));// 月，因为Calendar里的月是从0开始，所以要减1
        cal.set(Calendar.DATE, 1);// 日，设为一号
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());// 获得月初是几号
    }

    /**
     * 获得指定月第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static String getFirstDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最小天数
        int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最小天数
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String firstDayOfMonth = sdf.format(cal.getTime());
        return firstDayOfMonth;
    }

    public static String getFirstDayOfMonth(String date) {
        Date dt = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dt = df.parse(date);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        Calendar cal = Calendar.getInstance();
        assert dt != null;
        cal.setTime(dt);
        // 设置年份
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        // 设置月份
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
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
     * 获得指定月最后一天
     *
     * @param year
     * @param month
     * @return
     */
    public static String getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
        return lastDayOfMonth;
    }

    /* 取得上个月的第一天 */
    public static String getLastMonthDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));// 年
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));// 月，因为Calendar里的月是从0开始，所以要减1
        cal.set(Calendar.DATE, 1);// 日，设为一号
        cal.add(Calendar.MONTH, -1);// 月份减一，得到上个月的一号
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());// 获得月初是几号
    }

    /* 取得下个月的最后一天 */
    public static String getNextMonthEndDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));// 年
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));// 月，因为Calendar里的月是从0开始，所以要减1
        cal.set(Calendar.DATE, 1);// 日，设为一号
        cal.add(Calendar.MONTH, 2);// 月份加一，得到下下个月的一号
        cal.add(Calendar.DATE, -1);// 下下一个月减一为下个月最后一天
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());// 获得月末是几号
    }

    /**
     * 取得上个月的今天，当前日期-30
     */
    public static String getLastMoneyDay() {
        return DateUtils.getOpeDate(DateUtils.getSystemDate(), -30);
    }

    /* 取得指定月最后一天 */
    public static String getLastDayOfMonth(String date) {
        Date dt = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dt = df.parse(date);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        Calendar cal = Calendar.getInstance();
        assert dt != null;
        cal.setTime(dt);
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));// 年
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));// 月，因为Calendar里的月是从0开始，所以要减1
        cal.set(Calendar.DATE, 1);// 日，设为一号
        cal.add(Calendar.MONTH, 1);// 月份加一，得到下个月的一号
        cal.add(Calendar.DATE, -1);// 下一个月减一为本月最后一天
        return df.format(cal.getTime());// 获得月末是几号
    }

    /**
     * 获取某个时间段的所有天数集合(包含起始日期与终止日期)
     */
    public static List<String> getDayList(String starDate, String endDate) {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        List<String> dayList = new ArrayList<String>();
        if (starDate.equals(endDate)) {
            dayList.add(starDate);
        } else if (starDate.compareTo(endDate) < 0) {
            while (starDate.compareTo(endDate) <= 0) {
                dayList.add(starDate);
                try {
                    long l = stringToDate(starDate).getTime();
                    starDate = format.format(l + 3600 * 24 * 1000);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        } else {
            dayList.add(endDate);
        }
        return dayList;
    }

    /**
     * 判断时间是不是今天
     *
     * @param date
     * @return 是返回true，不是返回false
     */
    public static boolean isNow(Date date) {
        // 当前时间
        Date now = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        // 获取今天的日期
        String nowDay = sf.format(now);
        // 对比的时间
        String day = sf.format(date);
        return day.equals(nowDay);
    }

    /**
     * 日期转星期
     *
     * @param datetime
     * @return
     */
    public static String dateToWeek(String datetime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String[] weekDays = { "7", "1", "2", "3", "4", "5", "6" };
        Calendar cal = Calendar.getInstance(); // 获得一个日历
        Date datet = null;
        try {
            datet = f.parse(datetime);
            cal.setTime(datet);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     *      * 判断时间是否在时间段内
     *      * @param nowTime
     *      * @param beginTime
     *      * @param endTime
     *      * @return
     *      
     */
    public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        // 设置当前时间
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        // 设置开始时间
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        // 设置结束时间
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        // 处于开始时间之后，和结束时间之前的判断
        if (date.after(begin) && date.before(end) || begin.equals(date) || end.equals(date)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取日期是本年的第几周或者本月第几周
     *
     * @param today
     * @param type  1年2月
     * @return
     */
    public static int getweek(String today, Integer type) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(today);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        if (type == 1) {
            return calendar.get(Calendar.WEEK_OF_YEAR);
        } else {
            return calendar.get(Calendar.WEEK_OF_MONTH);
        }
    }

    public static int getYear(String today) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(today);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        Calendar calendar = Calendar.getInstance();
        // calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(String today) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(today);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 根据当前日期获得所在周的日期区间（周一和周日日期）
     * 
     * @param date
     * @return
     */
    public static List<String> dateToWeek(Date date) {
        List<String> dateWeekList = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // System.out.println("要计算日期为:" + sdf.format(cal.getTime())); // 输出要计算日期
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        String imptimeBegin = sdf.format(cal.getTime());
        // System.out.println("所在周星期一的日期：" + imptimeBegin);
        cal.add(Calendar.DATE, 6);
        String imptimeEnd = sdf.format(cal.getTime());
        // System.out.println("所在周星期日的日期：" + imptimeEnd);
        // return imptimeBegin + "," + imptimeEnd;
        // 区间内 所有的日期
        List<String> dayList = getDayList(imptimeBegin, imptimeEnd);
        return dayList;
    }

    // 获取上周周一日期
    public static Date getLastWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, -7);
        return cal.getTime();
    }

    // 获取这周周一日期
    public static Date getThisWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return cal.getTime();
    }

    // 获取下周周一日期
    public static Date getNextWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, 7);
        return cal.getTime();
    }

    // 下周周日日期
    public static Date getNextWeekSunday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, 13);
        return cal.getTime();
    }

    // 获取上周周日日期
    public static Date getLastWeekSunday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    /**
     * 获取当前年的第一天
     * 
     * @return
     */
    public static String getCurrYearFirst() {
        Calendar currCal = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, currCal.get(Calendar.YEAR));
        Date time = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(time);
    }

    /**
     * 获取当前年的最后一天
     * 
     * @return
     */
    public static String getCurrYearLast() {
        Calendar currCal = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, currCal.get(Calendar.YEAR));
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        Date time = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(time);

    }

    // 校验日期格式
    public static Date isDate(String dateStr) {
        Date date = null;
        try {
            if (dateStr.contains("-")) {
                date = DateUtils.stringToDate(dateStr);
            } else if (dateStr.contains("/")) {
                date = DateUtils.stringToDate(dateStr, "yyyy/MM/dd");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取过去12个月月份
     * 
     * @param date
     * @return
     */
    public static String[] getLatest12Month(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date parse = null;
        try {
            parse = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] months = new String[12];
        Calendar cal = Calendar.getInstance();
        cal.setTime(parse);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        // 加一行代码,否则3月重复
        cal.set(Calendar.DATE, 1);
        for (int i = 0; i < 12; i++) {
            String month = cal.get(Calendar.YEAR) + "-" + fillZero(cal.get(Calendar.MONTH) + 1);
            months[11 - i] = month;
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
        }
        return months;
    }

    /**
     * 格式化月份
     */
    public static String fillZero(int i) {
        String month = "";
        if (i < 10) {
            month = "0" + i;
        } else {
            month = String.valueOf(i);
        }
        return month;
    }

    /**
     * 获取某个时间段内所有月份
     * 
     * @param minDate
     * @param maxDate
     * @return
     * @throws ParseException
     */
    public static List<String> getMonthBetweenDates(String minDate, String maxDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date parse1 = null;
        Date parse2 = null;
        try {
            parse1 = sdf.parse(minDate);
            parse2 = sdf.parse(maxDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ArrayList<String> result = new ArrayList<String>();
        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();
        min.setTime(parse1);
        min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
        max.setTime(parse2);
        max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

        Calendar curr = min;
        while (curr.before(max)) {
            String format = sdf.format(curr.getTime());
            result.add(format);
            curr.add(Calendar.MONTH, 1);
        }
        return result;
    }

    public static void main(String[] args) {

        System.out.println(DateUtils.getSecDifference(new Date(),
                DateUtils.stringToDate("2021-10-28 11:00:00", "yyyy-MM-dd HH:mm:ss")));
    }

}
