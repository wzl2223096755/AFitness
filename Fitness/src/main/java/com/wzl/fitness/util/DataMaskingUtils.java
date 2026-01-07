package com.wzl.fitness.util;

import java.util.regex.Pattern;

/**
 * 敏感数据脱敏工具类
 * 用于在日志和响应中对敏感数据进行脱敏处理
 */
public final class DataMaskingUtils {
    
    private DataMaskingUtils() {
        // 工具类不允许实例化
    }
    
    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"
    );
    
    // 手机号正则表达式（中国大陆）
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(1[3-9]\\d)(\\d{4})(\\d{4})"
    );
    
    // 身份证号正则表达式
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
        "(\\d{6})(\\d{8})(\\d{3}[0-9Xx])"
    );
    
    // 银行卡号正则表达式
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile(
        "(\\d{4})(\\d+)(\\d{4})"
    );
    
    /**
     * 脱敏邮箱地址
     * 例如: user@example.com -> u***@example.com
     * 
     * @param email 邮箱地址
     * @return 脱敏后的邮箱地址
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }
        
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (localPart.length() <= 1) {
            return "*" + domain;
        } else if (localPart.length() <= 3) {
            return localPart.charAt(0) + "***" + domain;
        } else {
            return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + domain;
        }
    }
    
    /**
     * 脱敏手机号
     * 例如: 13812345678 -> 138****5678
     * 
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        
        // 保留前3位和后4位
        int length = phone.length();
        if (length >= 11) {
            return phone.substring(0, 3) + "****" + phone.substring(length - 4);
        } else {
            return phone.substring(0, 3) + "****" + phone.substring(length - 2);
        }
    }
    
    /**
     * 脱敏身份证号
     * 例如: 110101199001011234 -> 110101********1234
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        
        int length = idCard.length();
        if (length >= 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(length - 4);
        } else if (length >= 15) {
            return idCard.substring(0, 6) + "******" + idCard.substring(length - 3);
        } else {
            return idCard.substring(0, 2) + "****" + idCard.substring(length - 2);
        }
    }
    
    /**
     * 脱敏银行卡号
     * 例如: 6222021234567890123 -> 6222********0123
     * 
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }
        
        int length = bankCard.length();
        return bankCard.substring(0, 4) + "********" + bankCard.substring(length - 4);
    }
    
    /**
     * 脱敏用户名
     * 例如: username -> u******e
     * 
     * @param username 用户名
     * @return 脱敏后的用户名
     */
    public static String maskUsername(String username) {
        if (username == null || username.isEmpty()) {
            return username;
        }
        
        int length = username.length();
        if (length <= 2) {
            return username.charAt(0) + "*";
        } else if (length <= 4) {
            return username.charAt(0) + "**" + username.charAt(length - 1);
        } else {
            int maskLength = length - 2;
            return username.charAt(0) + "*".repeat(Math.min(maskLength, 6)) + username.charAt(length - 1);
        }
    }
    
    /**
     * 脱敏密码（完全隐藏）
     * 
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        return "********";
    }
    
    /**
     * 脱敏地址
     * 例如: 北京市朝阳区xxx街道xxx号 -> 北京市朝阳区***
     * 
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String maskAddress(String address) {
        if (address == null || address.length() < 6) {
            return address;
        }
        
        // 保留前6个字符
        int keepLength = Math.min(6, address.length() / 2);
        return address.substring(0, keepLength) + "***";
    }
    
    /**
     * 脱敏姓名
     * 例如: 张三 -> 张*
     *       张三丰 -> 张*丰
     * 
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        int length = name.length();
        if (length == 1) {
            return "*";
        } else if (length == 2) {
            return name.charAt(0) + "*";
        } else {
            return name.charAt(0) + "*".repeat(length - 2) + name.charAt(length - 1);
        }
    }
    
    /**
     * 脱敏IP地址
     * 例如: 192.168.1.100 -> 192.168.*.*
     * 
     * @param ip IP地址
     * @return 脱敏后的IP地址
     */
    public static String maskIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".*.*";
        }
        
        // IPv6 或其他格式
        if (ip.contains(":")) {
            int colonIndex = ip.indexOf(":");
            if (colonIndex > 0) {
                return ip.substring(0, colonIndex) + ":****";
            }
        }
        
        return ip;
    }
    
    /**
     * 自动检测并脱敏字符串中的敏感信息
     * 
     * @param text 包含敏感信息的文本
     * @return 脱敏后的文本
     */
    public static String autoMask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String result = text;
        
        // 脱敏邮箱
        result = EMAIL_PATTERN.matcher(result).replaceAll(match -> {
            String localPart = match.group(1);
            String domain = match.group(2);
            if (localPart.length() <= 1) {
                return "*@" + domain;
            }
            return localPart.charAt(0) + "***@" + domain;
        });
        
        // 脱敏手机号
        result = PHONE_PATTERN.matcher(result).replaceAll("$1****$3");
        
        // 脱敏身份证号
        result = ID_CARD_PATTERN.matcher(result).replaceAll("$1********$3");
        
        return result;
    }
}
