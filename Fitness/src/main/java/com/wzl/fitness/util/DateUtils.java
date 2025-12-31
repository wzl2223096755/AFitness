package com.wzl.fitness.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * 日期处理工具类
 * 提供日期格式化、解析、计算等功能
 */
public class DateUtils {
    // 默认日期时间格式
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // 默认日期格式
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    // 默认时间格式
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    
    /**
     * 格式化LocalDateTime为字符串
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_DATE_TIME_FORMAT);
    }
    
    /**
     * 格式化LocalDateTime为指定格式的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(pattern).format(dateTime);
    }
    
    /**
     * 格式化LocalDate为字符串
     */
    public static String format(LocalDate date) {
        return format(date, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 格式化LocalDate为指定格式的字符串
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(pattern).format(date);
    }
    
    /**
     * 解析字符串为LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, DEFAULT_DATE_TIME_FORMAT);
    }
    
    /**
     * 解析字符串为指定格式的LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 解析字符串为LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 解析字符串为指定格式的LocalDate
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 获取今天的开始时间（00:00:00）
     */
    public static LocalDateTime getTodayStart() {
        return LocalDate.now().atStartOfDay();
    }
    
    /**
     * 获取今天的结束时间（23:59:59）
     */
    public static LocalDateTime getTodayEnd() {
        return LocalDate.now().atTime(23, 59, 59);
    }
    
    /**
     * 获取本周的开始时间
     */
    public static LocalDateTime getWeekStart() {
        return LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
    }
    
    /**
     * 获取本周的结束时间
     */
    public static LocalDateTime getWeekEnd() {
        return LocalDate.now().with(DayOfWeek.SUNDAY).atTime(23, 59, 59);
    }
    
    /**
     * 获取本月的开始时间
     */
    public static LocalDateTime getMonthStart() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
    }
    
    /**
     * 获取本月的结束时间
     */
    public static LocalDateTime getMonthEnd() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
    }
    
    /**
     * 计算两个时间之间的天数差
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }
    
    /**
     * 计算两个时间之间的小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }
    
    /**
     * 计算两个时间之间的分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }
    
    /**
     * 将Date转换为LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * 将LocalDateTime转换为Date
     */
    public static Date localDateTimeToDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}