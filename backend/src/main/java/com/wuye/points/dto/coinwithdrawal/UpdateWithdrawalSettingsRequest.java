package com.wuye.points.dto.coinwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新提现设置请求")
public class UpdateWithdrawalSettingsRequest {

    @NotNull(message = "autoEnabled不能为空")
    @Schema(description = "是否开启自动提现", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean autoEnabled;

    @NotNull(message = "periodDays不能为空")
    @Min(value = 1, message = "periodDays最小为1")
    @Max(value = 365, message = "periodDays最大为365")
    @Schema(description = "自动提现周期天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer periodDays;
}
