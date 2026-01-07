package com.wzl.fitness.common;

/**
 * 统一响应码枚举类
 * 定义系统中所有的响应状态码和对应的消息
 */
public enum ResponseCode {
    // 成功状态码
    SUCCESS(200, "操作成功"),
    
    // 客户端错误状态码
    ERROR(400, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    VALIDATION_ERROR(400, "请求参数验证失败"),
    NOT_FOUND(404, "资源不存在"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "权限不足"),
    
    // 服务器错误状态码
    SERVER_ERROR(500, "系统异常"),
    SYSTEM_ERROR(500, "系统内部错误"),
    
    // 业务特定状态码
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    EMAIL_EXISTS(1003, "邮箱已存在"),
    LOGIN_FAILED(1004, "登录失败"),
    PASSWORD_ERROR(1005, "用户名或密码错误"),
    DEVICE_NOT_FOUND(1006, "设备不存在"),
    DEVICE_BIND_FAILED(1007, "设备绑定失败"),
    FITNESS_DATA_EXCEPTION(1008, "健身数据异常"),
    FITNESS_PLAN_NOT_FOUND(1009, "健身计划不存在"),
    HUAWEI_AUTH_FAILED(1010, "华为授权失败"),
    DATA_SYNC_FAILED(1011, "数据同步失败");
    
    private final int code;
    private final String message;
    
    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}