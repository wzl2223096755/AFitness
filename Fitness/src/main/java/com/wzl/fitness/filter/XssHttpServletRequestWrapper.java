package com.wzl.fitness.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * XSS防护请求包装器
 * 对请求参数和请求体进行XSS过滤
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    private byte[] cachedBody;
    
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
        // Event handlers
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
    
    public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 缓存请求体以便多次读取
        if (isJsonRequest(request)) {
            InputStream inputStream = request.getInputStream();
            this.cachedBody = StreamUtils.copyToByteArray(inputStream);
        }
    }
    
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return sanitize(value);
    }
    
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] sanitizedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitizedValues[i] = sanitize(values[i]);
        }
        return sanitizedValues;
    }
    
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        // 不过滤某些特定的头部
        if ("Authorization".equalsIgnoreCase(name) || 
            "Content-Type".equalsIgnoreCase(name) ||
            "Accept".equalsIgnoreCase(name)) {
            return value;
        }
        return sanitize(value);
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBody == null) {
            return super.getInputStream();
        }
        
        // 对JSON请求体进行XSS过滤
        String body = new String(cachedBody, StandardCharsets.UTF_8);
        String sanitizedBody = sanitizeJson(body);
        byte[] sanitizedBytes = sanitizedBody.getBytes(StandardCharsets.UTF_8);
        
        return new CachedServletInputStream(sanitizedBytes);
    }
    
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
    
    /**
     * 清理字符串中的XSS攻击代码
     */
    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        String sanitized = value;
        for (Pattern pattern : XSS_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }
        
        // HTML实体编码
        sanitized = sanitized
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
        
        return sanitized;
    }
    
    /**
     * 对JSON内容进行XSS过滤
     * 只过滤字符串值，不影响JSON结构
     */
    private String sanitizeJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        
        // 简单的JSON字符串值过滤
        // 匹配JSON中的字符串值并进行过滤
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        StringBuilder currentString = new StringBuilder();
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (escaped) {
                if (inString) {
                    currentString.append(c);
                } else {
                    result.append(c);
                }
                escaped = false;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                if (inString) {
                    currentString.append(c);
                } else {
                    result.append(c);
                }
                continue;
            }
            
            if (c == '"') {
                if (inString) {
                    // 结束字符串，进行XSS过滤
                    String sanitized = sanitizeJsonString(currentString.toString());
                    result.append('"').append(sanitized).append('"');
                    currentString = new StringBuilder();
                    inString = false;
                } else {
                    inString = true;
                }
                continue;
            }
            
            if (inString) {
                currentString.append(c);
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 对JSON字符串值进行XSS过滤
     */
    private String sanitizeJsonString(String value) {
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
     * 判断是否为JSON请求
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }
    
    /**
     * 缓存的ServletInputStream实现
     */
    private static class CachedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;
        
        public CachedServletInputStream(byte[] cachedBody) {
            this.inputStream = new ByteArrayInputStream(cachedBody);
        }
        
        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }
        
        @Override
        public boolean isReady() {
            return true;
        }
        
        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException("setReadListener is not supported");
        }
        
        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }
}
