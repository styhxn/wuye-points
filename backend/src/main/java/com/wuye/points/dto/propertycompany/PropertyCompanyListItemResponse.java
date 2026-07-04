package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业公司列表项响应")
public class PropertyCompanyListItemResponse {

    @Schema(description = "物业公司ID", example = "res_b7m9x1")
    private String id;

    @Schema(description = "物业公司名称", example = "万科物业")
    private String name;

    @Schema(description = "物业公司Logo地址", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "公司地址", example = "北京市朝阳区建国路88号")
    private String address;

    @Schema(description = "物业公司状态", allowableValues = {"active", "inactive", "pending"}, example = "active")
    private String status;

    @Schema(description = "小区数量", example = "5")
    private Integer communityCount;

    @Schema(description = "统计数据")
    private PropertyCompanyStats stats;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;

    @Schema(description = "更新时间", example = "2026-01-01T00:00:00.000Z")
    private String updatedAt;

    @Data
    @Builder
    @Schema(description = "物业公司统计数据")
    public static class PropertyCompanyStats {

        @Schema(description = "商户数量", example = "50")
        private Long merchantCount;

        @Schema(description = "居民数量", example = "1000")
        private Long residentCount;
    }
}
