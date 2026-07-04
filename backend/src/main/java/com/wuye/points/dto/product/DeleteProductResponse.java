package com.wuye.points.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "删除商品响应")
public class DeleteProductResponse {

    @Schema(description = "商品ID", example = "p_001")
    private String id;

    @Schema(description = "删除后的商品状态", example = "deleted")
    private String status;

    @Schema(description = "删除时间", example = "2025-01-15 10:30:00")
    private String deletedAt;
}
