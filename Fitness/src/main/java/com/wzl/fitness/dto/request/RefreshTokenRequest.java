package com.wzl.fitness.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求DTO
 */
@Data
@Schema(description = "刷新Token请求")
public class RefreshTokenRequest {

    @NotBlank(message = "刷新Token不能为空")
    @Schema(description = "刷新Token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String refreshToken;
    
    // 显式添加getter方法
    public String getRefreshToken() {
        return refreshToken;
    }
}