package com.hao.heji.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @date: 2020/11/19
 * @author: 锅得铁
 * #
 */
public class MyTimeUtils {
    //millisecond
    public static final String PATTERN_MILLISECOND = "yyy-MM-dd HH:mm:ss:SSS";
    //second
    public static final String PATTERN_SECOND = "yyy-MM-dd HH:mm:ss";
    //day
    public static final String PATTERN_DAY = "yyy-MM-dd";

    /**
     * 获取当前月第一天
     * date(timestring, modifier, modifier, ...)
     * 以 YYYY-MM-DD 格式返回日期。
     *
     * @param month
     * @return
     */
    public static String firstDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        // 设置月份
        calendar.set(Calendar.MONTH, month - 1);
        // 获取某月最小天数
        int firstDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最小天数
        calendar.set(Calendar.DAY_OF_MONTH, firstDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strFirstDay = sdf.format(calendar.getTime());
        return strFirstDay;
    }

    /**
     * date(timestring, modifier, modifier, ...)
     * 以 YYYY-MM-DD 格式返回日期。
     *
     * @param year
     * @param month
     * @return
     */
    public static String lastDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        // 设置月份
        calendar.set(Calendar.MONTH, month - 1);
        // 获取某月最大天数
        int lastDay = 0;
        //2月的平年瑞年天数
        if (month == 2) {
            lastDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
        } else {
            lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        // 设置日历中月份的最大天数
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strLastDay = sdf.format(calendar.getTime());
        return strLastDay;
    }

    public static int getMonthLastDay(int year, int month)
    {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);//把日期设置为当月第一天
        a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

}
