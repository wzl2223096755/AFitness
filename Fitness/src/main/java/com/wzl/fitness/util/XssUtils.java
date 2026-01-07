package com.wzl.fitness.util;

import java.util.regex.Pattern;

/**
 * XSS防护工具类
 * 提供静态方法用于清理和检测XSS攻击代码
 */
public final class XssUtils {
    
    private XssUtils() {
        // 工具类不允许实例化
    }
    
    // XSS攻击模式正则表达式
    private static final Pattern[] XSS_PATTERNS = {
        // Script tags
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // src='...'
        Pattern.compile("src[\r\n]*=[\r\n]*'(.*?)'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\r\n]*=[\r\n]*\"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // eval(...)
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // expression(...)
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // javascript:
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        // vbscript:
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        // Event handlers (onload, onerror, onclick, etc.)
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        // iframe
        Pattern.compile("<iframe(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // object
        Pattern.compile("<object(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // embed
        Pattern.compile("<embed(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // data: URLs
        Pattern.compile("data:", Pattern.CASE_INSENSITIVE)
    };
    
    /**
     * 检测字符串是否包含XSS攻击代码
     * 
     * @param value 要检测的字符串
     * @return 如果包含XSS攻击代码返回true，否则返回false
     */
    public static boolean containsXss(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 清理字符串中的XSS攻击代码
     * 
     * @param value 要清理的字符串
     * @return 清理后的字符串
     */
    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        String sanitized = value;
        for (Pattern pattern : XSS_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }
        
        return sanitized;
    }
    
    /**
     * 对字符串进行HTML实体编码
     * 
     * @param value 要编码的字符串
     * @return 编码后的字符串
     */
    public static String escapeHtml(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }
    
    /**
     * 清理并编码字符串
     * 先移除XSS攻击代码，再进行HTML实体编码
     * 
     * @param value 要处理的字符串
     * @return 处理后的字符串
     */
    public static String sanitizeAndEscape(String value) {
        return escapeHtml(sanitize(value));
    }
    
    /**
     * 清理字符串数组中的XSS攻击代码
     * 
     * @param values 要清理的字符串数组
     * @return 清理后的字符串数组
     */
    public static String[] sanitize(String[] values) {
        if (values == null) {
            return null;
        }
        
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitized[i] = sanitize(values[i]);
        }
        return sanitized;
    }
}
