package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 「我的分账所得」明细项：一订单 + 一角色一行。
 *
 * <p>应付来自 distribution_records（订单完成即落账），已到账来自 wechat_split_record
 * （按 owner_type + 收款主体匹配，见 {@code payoutStatus}）。</p>
 */
@Schema(description = "我的分账所得-明细")
@Data
@Builder
public class DistributionEarningsDetailItem {

    @Schema(description = "订单ID", example = "ord_abc123")
    private String orderId;

    @Schema(description = "订单号", example = "202611050001")
    private String orderNo;

    @Schema(description = "角色：COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER/BUILDING_LEADER/PROPERTY_MANAGER",
            example = "COORDINATOR")
    private String role;

    @Schema(description = "角色中文名", example = "统筹负责人")
    private String roleName;

    @Schema(description = "该笔分成对应的身份记录ID（coo_/sl_/il_/bl_/pmm_）", example = "coo_abc123")
    private String identityId;

    @Schema(description = "该身份在出参中的显示名（查不到姓名时为「历史身份」）", example = "张三")
    private String identityName;

    @Schema(description = "应付分成（元）", example = "12.50")
    private BigDecimal payableAmount;

    @Schema(description = "已到账（元）；未发起/未结算为 0", example = "12.50")
    private BigDecimal receivedAmount;

    @Schema(description = "到账状态：RECEIVED(已到账)/PROCESSING(分账处理中)/NOT_SPLIT(未发起分账)/"
            + "FAILED(分账失败)/SKIPPED(缺收款账户待补分)", example = "RECEIVED")
    private String payoutStatus;

    @Schema(description = "是否为历史身份（身份已停用或不属于当前物业）", example = "false")
    private Boolean isHistorical;

    @Schema(description = "身份状态：active(当前有效)/historical(历史)", example = "active")
    private String identityStatus;

    @Schema(description = "物业公司ID（仅展示）", example = "pc_abc123")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称（仅展示，查不到为空）", example = "万科物业")
    private String propertyCompanyName;

    @Schema(description = "分成落账时间（ISO UTC）", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
