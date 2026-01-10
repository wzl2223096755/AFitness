package com.wzl.fitness.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一分页响应类
 * 
 * @deprecated 请使用 {@link com.wzl.fitness.shared.common.PageResponse} 代替
 * 此类保留用于向后兼容，将在未来版本中移除
 */
@Deprecated
@Data
public class PageResponse<T> {
    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;
    private String timestamp;
    private boolean success;

    public PageResponse() {
        this.timestamp = LocalDateTime.now().toString();
        this.success = true;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setEmpty(page.isEmpty());
        return response;
    }

    public static <T> PageResponse<T> success(List<T> content, long totalElements, int currentPage, int pageSize) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(content);
        response.setTotalElements(totalElements);
        response.setCurrentPage(currentPage);
        response.setPageSize(pageSize);
        response.setTotalPages((int) Math.ceil((double) totalElements / pageSize));
        response.setFirst(currentPage == 0);
        response.setLast(currentPage >= response.getTotalPages() - 1);
        response.setEmpty(content.isEmpty());
        return response;
    }
    
    // 手动添加setter方法以确保编译通过
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
    
    public int getTotalPages() {
        return this.totalPages;
    }
}