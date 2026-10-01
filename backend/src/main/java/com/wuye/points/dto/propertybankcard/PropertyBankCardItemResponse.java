package com.wuye.points.dto.propertybankcard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "物业银行卡条目（账号脱敏）")
public class PropertyBankCardItemResponse {

    @Schema(description = "银行卡ID")
    private String id;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "开户行")
    private String bankName;

    @Schema(description = "支行")
    private String branchName;

    @Schema(description = "户名")
    private String accountName;

    @Schema(description = "账号（脱敏）")
    private String accountNoMasked;

    @Schema(description = "用途 points(积分钱)/reward(奖励金)")
    private String purpose;

    @Schema(description = "是否默认")
    private Boolean isDefault;

    @Schema(description = "状态 active/disabled")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
