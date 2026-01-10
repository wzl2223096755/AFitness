package com.wzl.fitness.shared.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 * 定义系统中所有的响应状态码和对应的消息
 */
@Getter
public enum ResponseCode {
    // 成功状态码
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "数据验证失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    
    // 服务器错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    
    // 业务错误码 1xxx - 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    EMAIL_EXISTS(1003, "邮箱已存在"),
    LOGIN_FAILED(1004, "登录失败"),
    INVALID_CREDENTIALS(1005, "用户名或密码错误"),
    ACCOUNT_DISABLED(1006, "账户已被禁用"),
    TOKEN_EXPIRED(1007, "令牌已过期"),
    TOKEN_INVALID(1008, "令牌无效"),
    
    // 业务错误码 2xxx - 训练相关
    TRAINING_RECORD_NOT_FOUND(2001, "训练记录不存在"),
    TRAINING_PLAN_NOT_FOUND(2002, "训练计划不存在"),
    INVALID_TRAINING_DATA(2003, "训练数据无效"),
    
    // 业务错误码 3xxx - 营养相关
    NUTRITION_RECORD_NOT_FOUND(3001, "营养记录不存在"),
    INVALID_NUTRITION_DATA(3002, "营养数据无效"),
    
    // 业务错误码 4xxx - 恢复评估相关
    RECOVERY_DATA_NOT_FOUND(4001, "恢复数据不存在"),
    INVALID_RECOVERY_DATA(4002, "恢复数据无效"),
    
    // 业务错误码 5xxx - 系统相关
    MODULE_NOT_FOUND(5001, "模块不存在"),
    MODULE_DISABLED(5002, "模块已禁用"),
    CACHE_ERROR(5003, "缓存操作失败"),
    EXPORT_ERROR(5004, "数据导出失败");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
