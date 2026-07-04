package com.wuye.points.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Schema(description = "分页响应")
public class PageResponse<T> {
    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "分页信息")
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
    @Schema(description = "分页信息")
    public static class Pagination {
        @Schema(description = "当前页码", example = "1")
        private int page;

        @Schema(description = "每页条数", example = "20")
        private int pageSize;

        @Schema(description = "总记录数", example = "100")
        private long total;

        @Schema(description = "总页数", example = "5")
        private int totalPages;
    }
}
