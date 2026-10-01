package com.wuye.points.dto.merchantad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "商家广告本周额度（含按天计价信息）")
public class MerchantAdQuotaResponse {

    private String weekStart;
    private String weekEnd;

    /** 本周免费额度（平台配置） */
    private Integer freeQuota;

    /** 管理员手动补充的免费额度 */
    private Integer purchasedQuota;

    /** 本周已发布广告总数 */
    private Integer usedCount;

    /** 本周免费额度已用条数 */
    private Integer freeUsedCount;

    /** 剩余可用额度条数（含免费+补充） */
    private Integer remainingCount;

    /** 免费额度剩余条数 */
    private Integer freeQuotaRemaining;

    /** 免费额度是否启用 */
    private Boolean freeQuotaEnabled;

    /** 每周免费条数（平台配置） */
    private Integer freeQuotaPerWeek;

    /** 广告单日价（元） */
    private BigDecimal dayPrice;
}
