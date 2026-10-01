package com.wuye.points.dto.propertyfee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "欠费催缴人数预览响应（发送前确认范围与金额）")
public class ArrearsReminderPreviewResponse {

    @Schema(description = "业务模板编码", example = "property_fee_arrears_reminder_v1")
    private String templateCode;

    @Schema(description = "预览快照令牌，发送时须原样回传以校验范围", example = "prev_xxx")
    private String previewToken;

    @Schema(description = "快照过期时间（ISO-8601，默认预览后 30 分钟）", example = "2026-08-13T14:10:00+08:00")
    private String expiresAt;

    @Schema(description = "唯一住户数（按 residentId 去重）", example = "120")
    private int recipientCount;

    @Schema(description = "欠费总额（已去重住户的欠费金额合计）", example = "85600.50")
    private BigDecimal totalArrearsAmount;

    @Schema(description = "应用内消息可触达人数（全员）", example = "120")
    private int inAppEligibleCount;

    @Schema(description = "微信可触达人数（已绑定微信且存在 openid）", example = "83")
    private int wechatEligibleCount;

    @Schema(description = "微信不可触达人数（将回退应用内消息）", example = "37")
    private int wechatIneligibleCount;

    @Schema(description = "被去重的欠费记录数（总欠费记录数 - 唯一住户数）", example = "5")
    private int duplicateResidentCount;
}
