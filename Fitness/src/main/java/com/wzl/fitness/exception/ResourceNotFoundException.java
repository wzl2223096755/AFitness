package com.wzl.fitness.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resource) {
        super(404, resource + "不存在");
    }
    
    public ResourceNotFoundException(String resource, Long id) {
        super(404, resource + "不存在，ID: " + id);
    }
    
    public ResourceNotFoundException(String resource, String identifier) {
        super(404, resource + "不存在，标识符: " + identifier);
    }
}