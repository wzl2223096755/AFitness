package com.wzl.fitness.exception;

import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.ResponseCode;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 异常处理属性测试
 * 
 * **Property 8: 异常处理统一性**
 * *For any* API请求异常，应返回统一格式的错误响应（包含code、message、data字段）
 * 
 * **Validates: Requirements 3.2**
 * 
 * Feature: project-evaluation, Property 8: 异常处理统一性
 */
public class ExceptionHandlingPropertyTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    /**
     * Property 8: 异常处理统一性 - BusinessException返回统一格式
     * 
     * 对于任意BusinessException，处理后的响应应包含code、message字段，且格式统一
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property 8: BusinessException返回统一格式响应")
    void businessExceptionReturnsUniformResponse(
            @ForAll @IntRange(min = 400, max = 599) int errorCode,
            @ForAll @StringLength(min = 1, max = 100) String errorMessage) {
        
        // Given
        BusinessException exception = new BusinessException(errorCode, errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);
        
        // Then - verify uniform response format
        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getBody(), "响应体不应为空");
        
        ApiResponse<Void> apiResponse = response.getBody();
        verifyUniformResponseFormat(apiResponse, errorCode, errorMessage);
    }

    /**
     * Property 8: 异常处理统一性 - IllegalArgumentException返回统一格式
     * 
     * 对于任意IllegalArgumentException，处理后的响应应包含code、message字段
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property 8: IllegalArgumentException返回统一格式响应")
    void illegalArgumentExceptionReturnsUniformResponse(
            @ForAll @StringLength(min = 1, max = 100) String errorMessage) {
        
        // Given
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleIllegalArgumentException(exception);
        
        // Then
        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getBody(), "响应体不应为空");
        
        ApiResponse<Void> apiResponse = response.getBody();
        assertEquals(ResponseCode.PARAM_ERROR.getCode(), apiResponse.getCode(),
                "IllegalArgumentException应返回PARAM_ERROR错误码");
        assertNotNull(apiResponse.getMessage(), "响应消息不应为空");
        assertFalse(apiResponse.isSuccess(), "错误响应的success应为false");
    }

    /**
     * Property 8: 异常处理统一性 - IllegalStateException返回统一格式
     * 
     * 对于任意IllegalStateException，处理后的响应应包含code、message字段
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property 8: IllegalStateException返回统一格式响应")
    void illegalStateExceptionReturnsUniformResponse(
            @ForAll @StringLength(min = 1, max = 100) String errorMessage) {
        
        // Given
        IllegalStateException exception = new IllegalStateException(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleIllegalStateException(exception);
        
        // Then
        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getBody(), "响应体不应为空");
        
        ApiResponse<Void> apiResponse = response.getBody();
        assertEquals(ResponseCode.PARAM_ERROR.getCode(), apiResponse.getCode(),
                "IllegalStateException应返回PARAM_ERROR错误码");
        assertNotNull(apiResponse.getMessage(), "响应消息不应为空");
        assertFalse(apiResponse.isSuccess(), "错误响应的success应为false");
    }

    /**
     * Property 8: 异常处理统一性 - AccessDeniedException返回统一格式
     * 
     * 对于任意AccessDeniedException，处理后的响应应返回FORBIDDEN错误码
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property 8: AccessDeniedException返回统一格式响应")
    void accessDeniedExceptionReturnsUniformResponse(
            @ForAll @StringLength(min = 1, max = 100) String errorMessage) {
        
        // Given
        AccessDeniedException exception = new AccessDeniedException(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAccessDeniedException(exception);
        
        // Then
        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getBody(), "响应体不应为空");
        
        ApiResponse<Void> apiResponse = response.getBody();
        assertEquals(ResponseCode.FORBIDDEN.getCode(), apiResponse.getCode(),
                "AccessDeniedException应返回FORBIDDEN错误码");
        assertNotNull(apiResponse.getMessage(), "响应消息不应为空");
        assertFalse(apiResponse.isSuccess(), "错误响应的success应为false");
    }

    /**
     * Property 8: 异常处理统一性 - BadCredentialsException返回统一格式
     * 
     * 对于任意BadCredentialsException，处理后的响应应返回UNAUTHORIZED错误码
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property 8: BadCredentialsException返回统一格式响应")
    void badCredentialsExceptionReturnsUniformResponse(
            @ForAll @StringLength(min = 1, max = 100) String errorMessage) {
        
        // Given
        BadCredentialsException exception = new BadCredentialsException(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBadCredentialsException(exception);
        
        // Then
        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getBody(), "响应体不应为空");
        
        ApiResponse<Void> apiResponse = response.getBody();
        assertEquals(ResponseCode.UNAUTHORIZED.getCode(), apiResponse.getCode(),
                "BadCredentialsException应返回UNAUTHORIZED错误码");
        assertNotNull(apiResponse.getMessage(), "响应消息不应为空");
        assertFalse(apiResponse.isSuccess(), "错误响应的success应为false");
    }

    /**
     * Property 8: 异常处理统一性 - RuntimeException返回统一格式（隐藏详细错误）
     * 
     * 对于任意RuntimeException，处理后的响应应返回SERVER_ERROR错误码，且不暴露详细错误信息
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property 8: RuntimeException返回统一格式响应且隐藏详细错误")
    void runtimeExceptionReturnsUniformResponseWithHiddenDetails(
            @ForAll @StringLength(min = 1, max = 100) String errorMessage) {
        
        // Given
        RuntimeException exception = new RuntimeException(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleRuntimeException(exception);
        
        // Then
        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getBody(), "响应体不应为空");
        
        ApiResponse<Void> apiResponse = response.getBody();
        assertEquals(ResponseCode.SERVER_ERROR.getCode(), apiResponse.getCode(),
                "RuntimeException应返回SERVER_ERROR错误码");
        // 验证不暴露详细错误信息
        assertFalse(apiResponse.getMessage().contains(errorMessage),
                "RuntimeException响应不应暴露详细错误信息");
        assertEquals("服务器内部错误", apiResponse.getMessage(),
                "RuntimeException应返回通用错误消息");
        assertFalse(apiResponse.isSuccess(), "错误响应的success应为false");
    }

    /**
     * Property 8: 异常处理统一性 - Exception返回统一格式（隐藏详细错误）
     * 
     * 对于任意Exception，处理后的响应应返回SERVER_ERROR错误码，且不暴露详细错误信息
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property 8: Exception返回统一格式响应且隐藏详细错误")
    void exceptionReturnsUniformResponseWithHiddenDetails(
            @ForAll @StringLength(min = 1, max = 100) String errorMessage) {
        
        // Given
        Exception exception = new Exception(errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleException(exception);
        
        // Then
        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getBody(), "响应体不应为空");
        
        ApiResponse<Void> apiResponse = response.getBody();
        assertEquals(ResponseCode.SERVER_ERROR.getCode(), apiResponse.getCode(),
                "Exception应返回SERVER_ERROR错误码");
        // 验证不暴露详细错误信息
        assertFalse(apiResponse.getMessage().contains(errorMessage),
                "Exception响应不应暴露详细错误信息");
        assertEquals("服务器内部错误", apiResponse.getMessage(),
                "Exception应返回通用错误消息");
        assertFalse(apiResponse.isSuccess(), "错误响应的success应为false");
    }

    /**
     * Property 8: 异常处理统一性 - 所有异常响应都包含timestamp
     * 
     * 对于任意异常，处理后的响应应包含timestamp字段
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property 8: 所有异常响应都包含timestamp")
    void allExceptionResponsesContainTimestamp(
            @ForAll @IntRange(min = 400, max = 599) int errorCode,
            @ForAll @StringLength(min = 1, max = 50) String errorMessage) {
        
        // Given
        BusinessException exception = new BusinessException(errorCode, errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);
        
        // Then
        assertNotNull(response.getBody(), "响应体不应为空");
        assertNotNull(response.getBody().getTimestamp(), "响应应包含timestamp字段");
    }

    /**
     * Property 8: 异常处理统一性 - 错误码范围验证
     * 
     * 对于任意异常，返回的错误码应在有效范围内（200-599或业务错误码1000+）
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    @Label("Property 8: 错误码在有效范围内")
    void errorCodeIsInValidRange(
            @ForAll @IntRange(min = 400, max = 599) int errorCode,
            @ForAll @StringLength(min = 1, max = 50) String errorMessage) {
        
        // Given
        BusinessException exception = new BusinessException(errorCode, errorMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);
        
        // Then
        int responseCode = response.getBody().getCode();
        boolean isValidHttpCode = responseCode >= 200 && responseCode < 600;
        boolean isValidBusinessCode = responseCode >= 1000;
        
        assertTrue(isValidHttpCode || isValidBusinessCode,
                String.format("错误码 %d 应在有效范围内", responseCode));
    }

    // ========== Helper Methods ==========

    /**
     * 验证统一响应格式
     */
    private void verifyUniformResponseFormat(ApiResponse<?> response, int expectedCode, String expectedMessage) {
        assertNotNull(response, "ApiResponse不应为空");
        assertEquals(expectedCode, response.getCode(), 
                String.format("错误码应为 %d", expectedCode));
        assertEquals(expectedMessage, response.getMessage(),
                String.format("错误消息应为 %s", expectedMessage));
        assertFalse(response.isSuccess(), "错误响应的success应为false");
        assertNotNull(response.getTimestamp(), "响应应包含timestamp");
    }
}
