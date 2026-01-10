package com.wzl.fitness.modules.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 * 用于模块间传输用户信息，不暴露Entity对象
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String gender;
    private Integer age;
    private Double height;
    private Double weight;
    private String role;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
