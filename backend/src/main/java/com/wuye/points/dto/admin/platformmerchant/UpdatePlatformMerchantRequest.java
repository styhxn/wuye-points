package com.wuye.points.dto.admin.platformmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新平台商家请求")
public class UpdatePlatformMerchantRequest {

    @Schema(description = "商家名称", example = "好邻居超市")
    private String name;

    @Schema(description = "品类", example = "超市")
    private String category;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "地址", example = "朝阳区建国路88号")
    private String address;

    @Schema(description = "封面图URL", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "商家描述", example = "社区便利超市")
    private String description;

    @Schema(description = "营业时间", example = "08:00-22:00")
    private String businessHours;

    @Schema(description = "状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "PENDING"})
    private String status;
}
