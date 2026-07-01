package com.wuye.points.dto.coin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HideCoinRequest {

    @NotNull(message = "hidden不能为空")
    private Boolean hidden;
}
