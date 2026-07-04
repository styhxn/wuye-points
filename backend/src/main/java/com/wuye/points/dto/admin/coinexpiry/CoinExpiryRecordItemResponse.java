package com.wuye.points.dto.admin.coinexpiry;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "积分过期记录项响应")
public class CoinExpiryRecordItemResponse {

    @Schema(description = "记录ID", example = "1001")
    private String id;

    @Schema(description = "住户ID", example = "RES001")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "物业公司ID", example = "PROP001")
    private String propertyCompanyId;

    @Schema(description = "过期金额", example = "100.00")
    private BigDecimal expiredAmount;

    @Schema(description = "转入公共池金额", example = "80.00")
    private BigDecimal poolInAmount;

    @Schema(description = "过期时间", example = "2026-01-01 00:00:00")
    private String expiredAt;
}
