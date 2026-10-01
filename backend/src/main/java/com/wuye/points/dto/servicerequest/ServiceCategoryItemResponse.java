package com.wuye.points.dto.servicerequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "服务分类项")
public class ServiceCategoryItemResponse {

    @Schema(description = "分类ID")
    private String id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "关键词")
    private List<String> keywords;

    @Schema(description = "排序")
    private Integer sortOrder;
}
