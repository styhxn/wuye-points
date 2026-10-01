package com.wuye.points.dto.couriermanager;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "我的快递负责人工作台响应")
public class MyCourierManagerResponse {

    @Schema(description = "负责人ID", example = "cm_xxx")
    private String id;

    @Schema(description = "业主ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;

    @Schema(description = "小区ID", example = "com_xxx")
    private String communityId;

    @Schema(description = "负责人姓名", example = "张三")
    private String name;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "负责区域", example = "1-3号楼")
    private String responsibleArea;

    @Schema(description = "配送时间配置JSON", example = "{\"start\":\"08:00\",\"end\":\"20:00\"}")
    private String deliveryTimeConfig;

    @Schema(description = "快递员数量", example = "5")
    private Long courierCount;

    @Schema(description = "可提现金额", example = "1200.00")
    private BigDecimal withdrawableAmount;

    @Schema(description = "累计已提现金额", example = "500.00")
    private BigDecimal totalWithdrawn;

    @Schema(description = "是否被管理员阻止提现", example = "false")
    private Boolean withdrawalBlocked;

    @Schema(description = "状态 code", example = "active")
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;
}
