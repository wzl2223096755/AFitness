package com.wzl.fitness.modules.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 用户基础信息DTO
 * 用于跨模块引用时传递精简的用户信息
 * 
 * @see Requirements 2.5 - 模块接口使用DTO进行数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBasicInfo {
    
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
}
