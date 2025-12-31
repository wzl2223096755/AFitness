package com.wzl.fitness.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String accessToken;

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌过期时间（秒）", example = "86400")
    private Long expiresIn;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "用户角色", example = "ROLE_ADMIN")
    private String role;
    
    // 显式添加静态builder方法
    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }
    
    public static class LoginResponseBuilder {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private Long expiresIn = 86400L;
        private Long userId;
        private String username;
        private String role;
        
        public LoginResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public LoginResponseBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }
        
        public LoginResponseBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }
        
        public LoginResponseBuilder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }
        
        public LoginResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public LoginResponseBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public LoginResponseBuilder role(String role) {
            this.role = role;
            return this;
        }
        
        public LoginResponse build() {
            LoginResponse response = new LoginResponse();
            response.accessToken = this.accessToken;
            response.refreshToken = this.refreshToken;
            response.tokenType = this.tokenType;
            response.expiresIn = this.expiresIn;
            response.userId = this.userId;
            response.username = this.username;
            response.role = this.role;
            return response;
        }
    }
}