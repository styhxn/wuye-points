package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "隐藏物业币请求")
public class HideCoinRequest {

    @NotNull(message = "hidden不能为空")
    @Schema(description = "是否隐藏", required = true, example = "true")
    private Boolean hidden;
}
