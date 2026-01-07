package com.wzl.fitness.validation;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 输入验证属性测试
 * 
 * **Property 3: 输入验证有效性**
 * *For any* 用户输入, 验证规则应正确识别并拒绝无效输入，同时允许所有有效输入通过
 * 
 * **Validates: Requirements 8.1, 8.4**
 * 
 * Feature: system-optimization-95, Property 3: 输入验证有效性
 */
public class InputValidationPropertyTest {

    private final NoXssValidator xssValidator = new NoXssValidator();

    /**
     * Property 1: XSS检测 - 包含script标签的输入应被拒绝
     * 
     * 对于任意包含<script>标签的字符串，XSS验证器应返回false
     * 
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 100)
    @Label("Property 1: XSS检测 - 包含script标签的输入应被拒绝")
    void inputWithScriptTagShouldBeRejected(
            @ForAll @StringLength(min = 0, max = 50) String prefix,
            @ForAll @StringLength(min = 0, max = 50) String content,
            @ForAll @StringLength(min = 0, max = 50) String suffix) {
        
        // 构造包含script标签的输入
        String maliciousInput = prefix + "<script>" + content + "</script>" + suffix;
        
        // XSS验证器应拒绝此输入
        boolean isValid = xssValidator.isValid(maliciousInput, null);
        assertFalse(isValid, 
                String.format("包含<script>标签的输入应被拒绝: %s", maliciousInput));
    }

    /**
     * Property 2: XSS检测 - 包含javascript:协议的输入应被拒绝
     * 
     * 对于任意包含javascript:协议的字符串，XSS验证器应返回false
     * 
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 100)
    @Label("Property 2: XSS检测 - 包含javascript:协议的输入应被拒绝")
    void inputWithJavascriptProtocolShouldBeRejected(
            @ForAll @StringLength(min = 0, max = 50) String prefix,
            @ForAll @StringLength(min = 0, max = 50) String suffix) {
        
        // 构造包含javascript:协议的输入
        String maliciousInput = prefix + "javascript:" + suffix;
        
        // XSS验证器应拒绝此输入
        boolean isValid = xssValidator.isValid(maliciousInput, null);
        assertFalse(isValid, 
                String.format("包含javascript:协议的输入应被拒绝: %s", maliciousInput));
    }

    /**
     * Property 3: XSS检测 - 包含事件处理器的输入应被拒绝
     * 
     * 对于任意包含on*事件处理器的字符串，XSS验证器应返回false
     * 
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 100)
    @Label("Property 3: XSS检测 - 包含事件处理器的输入应被拒绝")
    void inputWithEventHandlerShouldBeRejected(
            @ForAll @StringLength(min = 0, max = 30) String prefix,
            @ForAll("eventHandlers") String eventHandler,
            @ForAll @StringLength(min = 0, max = 30) String suffix) {
        
        // 构造包含事件处理器的输入
        String maliciousInput = prefix + eventHandler + "=alert(1)" + suffix;
        
        // XSS验证器应拒绝此输入
        boolean isValid = xssValidator.isValid(maliciousInput, null);
        assertFalse(isValid, 
                String.format("包含事件处理器的输入应被拒绝: %s", maliciousInput));
    }

    @Provide
    Arbitrary<String> eventHandlers() {
        return Arbitraries.of("onclick", "onload", "onerror", "onmouseover", "onfocus", "onblur");
    }

    /**
     * Property 4: XSS检测 - 纯文本输入应被接受
     * 
     * 对于任意不包含XSS攻击代码的纯文本字符串，XSS验证器应返回true
     * 
     * **Validates: Requirements 8.1**
     */
    @Property(tries = 100)
    @Label("Property 4: XSS检测 - 纯文本输入应被接受")
    void plainTextInputShouldBeAccepted(
            @ForAll("safeStrings") String safeInput) {
        
        // XSS验证器应接受纯文本输入
        boolean isValid = xssValidator.isValid(safeInput, null);
        assertTrue(isValid, 
                String.format("纯文本输入应被接受: %s", safeInput));
    }

    @Provide
    Arbitrary<String> safeStrings() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars(' ', '.', ',', '!', '?', '-', '_', '@', '#', '$', '%', '&', '*', '(', ')', '+', '=')
                .ofMinLength(0)
                .ofMaxLength(100);
    }

    /**
     * Property 5: XSS检测 - 空值和空字符串应被接受
     * 
     * null和空字符串应该通过XSS验证（由其他验证器处理）
     * 
     * **Validates: Requirements 8.1**
     */
    @Property(tries = 10)
    @Label("Property 5: XSS检测 - 空值和空字符串应被接受")
    void nullAndEmptyStringShouldBeAccepted() {
        // null应该通过验证
        assertTrue(xssValidator.isValid(null, null), "null应被接受");
        
        // 空字符串应该通过验证
        assertTrue(xssValidator.isValid("", null), "空字符串应被接受");
    }

    /**
     * Property 6: XSS检测 - 包含iframe标签的输入应被拒绝
     * 
     * 对于任意包含<iframe>标签的字符串，XSS验证器应返回false
     * 
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 100)
    @Label("Property 6: XSS检测 - 包含iframe标签的输入应被拒绝")
    void inputWithIframeShouldBeRejected(
            @ForAll @StringLength(min = 0, max = 50) String prefix,
            @ForAll @StringLength(min = 0, max = 50) String src,
            @ForAll @StringLength(min = 0, max = 50) String suffix) {
        
        // 构造包含iframe标签的输入
        String maliciousInput = prefix + "<iframe src=\"" + src + "\">" + suffix;
        
        // XSS验证器应拒绝此输入
        boolean isValid = xssValidator.isValid(maliciousInput, null);
        assertFalse(isValid, 
                String.format("包含<iframe>标签的输入应被拒绝: %s", maliciousInput));
    }

    /**
     * Property 7: XSS检测 - 包含eval函数的输入应被拒绝
     * 
     * 对于任意包含eval()函数调用的字符串，XSS验证器应返回false
     * 
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 100)
    @Label("Property 7: XSS检测 - 包含eval函数的输入应被拒绝")
    void inputWithEvalShouldBeRejected(
            @ForAll @StringLength(min = 0, max = 50) String prefix,
            @ForAll @StringLength(min = 0, max = 50) String code,
            @ForAll @StringLength(min = 0, max = 50) String suffix) {
        
        // 构造包含eval函数的输入
        String maliciousInput = prefix + "eval(" + code + ")" + suffix;
        
        // XSS验证器应拒绝此输入
        boolean isValid = xssValidator.isValid(maliciousInput, null);
        assertFalse(isValid, 
                String.format("包含eval()函数的输入应被拒绝: %s", maliciousInput));
    }

    /**
     * Property 8: XSS检测 - 大小写变体应被检测
     * 
     * XSS攻击代码的大小写变体也应被正确检测
     * 
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 50)
    @Label("Property 8: XSS检测 - 大小写变体应被检测")
    void caseVariantsShouldBeDetected(
            @ForAll("scriptTagVariants") String scriptTag) {
        
        // 构造包含大小写变体script标签的输入
        String maliciousInput = scriptTag + "alert(1)</script>";
        
        // XSS验证器应拒绝此输入
        boolean isValid = xssValidator.isValid(maliciousInput, null);
        assertFalse(isValid, 
                String.format("大小写变体的script标签应被拒绝: %s", maliciousInput));
    }

    @Provide
    Arbitrary<String> scriptTagVariants() {
        return Arbitraries.of(
                "<script>", "<SCRIPT>", "<Script>", "<sCrIpT>",
                "<ScRiPt>", "<SCRipt>", "<scriPT>"
        );
    }

    /**
     * Property 9: SafeHtml验证 - 不允许HTML时应拒绝所有HTML标签
     * 
     * 当allowBasicTags为false时，任何HTML标签都应被拒绝
     * 
     * **Validates: Requirements 8.1**
     */
    @Property(tries = 100)
    @Label("Property 9: SafeHtml验证 - 不允许HTML时应拒绝所有HTML标签")
    void htmlTagsShouldBeRejectedWhenNotAllowed(
            @ForAll("htmlTags") String tag,
            @ForAll @StringLength(min = 0, max = 30) String content) {
        
        SafeHtmlValidator validator = new SafeHtmlValidator();
        // 模拟allowBasicTags = false的情况
        
        // 构造包含HTML标签的输入
        String htmlInput = "<" + tag + ">" + content + "</" + tag + ">";
        
        // 验证器应拒绝此输入（因为默认不允许HTML标签）
        boolean isValid = validator.isValid(htmlInput, null);
        assertFalse(isValid, 
                String.format("不允许HTML时应拒绝HTML标签: %s", htmlInput));
    }

    @Provide
    Arbitrary<String> htmlTags() {
        return Arbitraries.of("div", "span", "a", "img", "table", "form", "input", "button");
    }

    /**
     * Property 10: XSS检测 - 包含data:协议的输入应被拒绝
     * 
     * 对于任意包含data:协议的字符串，XSS验证器应返回false
     * 
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 100)
    @Label("Property 10: XSS检测 - 包含data:协议的输入应被拒绝")
    void inputWithDataProtocolShouldBeRejected(
            @ForAll @StringLength(min = 0, max = 50) String prefix,
            @ForAll @StringLength(min = 0, max = 50) String suffix) {
        
        // 构造包含data:协议的输入
        String maliciousInput = prefix + "data:" + suffix;
        
        // XSS验证器应拒绝此输入
        boolean isValid = xssValidator.isValid(maliciousInput, null);
        assertFalse(isValid, 
                String.format("包含data:协议的输入应被拒绝: %s", maliciousInput));
    }
}
