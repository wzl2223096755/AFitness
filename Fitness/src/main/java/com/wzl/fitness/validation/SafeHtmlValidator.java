package com.wzl.fitness.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 安全HTML验证器实现
 */
public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, String> {
    
    private boolean allowBasicTags;
    
    // 允许的基本HTML标签
    private static final Set<String> BASIC_TAGS = Set.of(
        "b", "i", "u", "br", "p", "strong", "em", "span"
    );
    
    // HTML标签匹配模式
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("</?([a-zA-Z][a-zA-Z0-9]*)[^>]*>");
    
    @Override
    public void initialize(SafeHtml constraintAnnotation) {
        this.allowBasicTags = constraintAnnotation.allowBasicTags();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        
        // 首先检查XSS
        if (NoXssValidator.containsXss(value)) {
            return false;
        }
        
        // 如果不允许任何HTML标签
        if (!allowBasicTags) {
            return !HTML_TAG_PATTERN.matcher(value).find();
        }
        
        // 如果允许基本标签，检查是否只包含允许的标签
        Matcher matcher = HTML_TAG_PATTERN.matcher(value);
        while (matcher.find()) {
            String tagName = matcher.group(1).toLowerCase();
            if (!BASIC_TAGS.contains(tagName)) {
                return false;
            }
        }
        
        return true;
    }
}
