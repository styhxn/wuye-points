package com.wuye.points.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "分页请求")
public class PageRequest {
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码", example = "1")
    private int page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    @Schema(description = "每页条数", example = "20")
    private int pageSize = 20;

    @Schema(description = "排序方式", example = "-createdAt")
    private String sort = "-createdAt";

    /**
     * 获取 Spring Data 的 Pageable 对象（页码从0开始）
     */
    public org.springframework.data.domain.Pageable toPageable() {
        String sortField = sort;
        org.springframework.data.domain.Sort.Direction direction =
                org.springframework.data.domain.Sort.Direction.DESC;
        if (sortField != null && sortField.startsWith("-")) {
            sortField = sortField.substring(1);
            direction = org.springframework.data.domain.Sort.Direction.DESC;
        } else if (sortField != null && sortField.startsWith("+")) {
            sortField = sortField.substring(1);
            direction = org.springframework.data.domain.Sort.Direction.ASC;
        }
        // camelCase -> snake_case
        String snakeField = sortField.replaceAll("([A-Z])", "_$1").toLowerCase();
        return org.springframework.data.domain.PageRequest.of(
                page - 1, pageSize,
                org.springframework.data.domain.Sort.by(direction, snakeField)
        );
    }
}
