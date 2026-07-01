package com.wuye.points.dto.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> list;
    private Pagination pagination;

    public PageResponse() {}

    public PageResponse(List<T> list, Pagination pagination) {
        this.list = list;
        this.pagination = pagination;
    }

    public static <T> PageResponse<T> of(List<T> list, int page, int pageSize, long total) {
        PageResponse<T> response = new PageResponse<>();
        response.setList(list);
        Pagination p = new Pagination();
        p.setPage(page);
        p.setPageSize(pageSize);
        p.setTotal(total);
        p.setTotalPages((int) Math.ceil((double) total / pageSize));
        response.setPagination(p);
        return response;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return of(page.getContent(), (int) page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Data
    public static class Pagination {
        private int page;
        private int pageSize;
        private long total;
        private int totalPages;
    }
}
