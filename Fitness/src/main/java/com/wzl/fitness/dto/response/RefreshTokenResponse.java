package com.wzl.fitness.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新Token响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新Token响应")
public class RefreshTokenResponse {

    @Schema(description = "新的访问令牌", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String accessToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌过期时间（秒）", example = "86400")
    private Long expiresIn;
    
    // 显式添加静态builder方法
    public static RefreshTokenResponseBuilder builder() {
        return new RefreshTokenResponseBuilder();
    }
    
    public static class RefreshTokenResponseBuilder {
        private String accessToken;
        private String tokenType = "Bearer";
        private Long expiresIn = 86400L;
        
        public RefreshTokenResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public RefreshTokenResponseBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }
        
        public RefreshTokenResponseBuilder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }
        
        public RefreshTokenResponse build() {
            RefreshTokenResponse response = new RefreshTokenResponse();
            response.accessToken = this.accessToken;
            response.tokenType = this.tokenType;
            response.expiresIn = this.expiresIn;
            return response;
        }
    }
}