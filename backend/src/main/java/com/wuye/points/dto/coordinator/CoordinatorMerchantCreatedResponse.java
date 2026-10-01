package com.wuye.points.dto.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "统筹新增商户响应")
public class CoordinatorMerchantCreatedResponse {

    @Schema(description = "商家ID", example = "mch_xxxx")
    private String id;

    @Schema(description = "平台商家ID", example = "pm_xxxx")
    private String platformMerchantId;

    @Schema(description = "物业公司ID", example = "pc_xxxx")
    private String propertyCompanyId;

    @Schema(description = "统筹ID", example = "coo_xxxx")
    private String coordinatorId;

    @Schema(description = "商家名称", example = "示例商家")
    private String name;

    @Schema(description = "商家分类", example = "外卖")
    private String category;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "地址", example = "示例路1号")
    private String address;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "运营状态", example = "active")
    private String status;
}
