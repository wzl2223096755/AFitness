package com.wzl.fitness.shared.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一分页响应类
 * 用于封装分页查询的响应数据
 */
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
}
