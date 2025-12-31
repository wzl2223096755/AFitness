package com.wzl.fitness.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 统一分页请求DTO
 * 用于所有列表接口的分页参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    /**
     * 页码（从0开始）
     */
    @Min(value = 0, message = "页码不能小于0")
    @Builder.Default
    private int page = 0;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    @Builder.Default
    private int size = 20;
    
    /**
     * 排序字段
     */
    private String sortBy;
    
    /**
     * 排序方向 (asc/desc)
     */
    @Builder.Default
    private String sortDirection = "desc";
    
    /**
     * 转换为Spring Data Pageable对象
     */
    public Pageable toPageable() {
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC;
            sort = Sort.by(direction, sortBy);
        }
        return org.springframework.data.domain.PageRequest.of(page, size, sort);
    }
    
    /**
     * 转换为带默认排序的Pageable对象
     */
    public Pageable toPageable(String defaultSortBy) {
        String effectiveSortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : defaultSortBy;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, effectiveSortBy);
        return org.springframework.data.domain.PageRequest.of(page, size, sort);
    }
    
    /**
     * 创建默认分页请求
     */
    public static PageRequest defaultRequest() {
        return PageRequest.builder()
                .page(0)
                .size(20)
                .sortDirection("desc")
                .build();
    }
}
