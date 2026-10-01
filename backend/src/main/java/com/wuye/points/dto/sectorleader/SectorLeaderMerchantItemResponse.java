package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "板块负责人-本板块商家条目")
public class SectorLeaderMerchantItemResponse {

    @Schema(description = "商家ID")
    private String id;

    @Schema(description = "商家名称")
    private String name;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "联系手机号")
    private String contactPhone;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "营业状态")
    private String status;
}
