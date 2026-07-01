package com.wuye.points.dto.coin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnfreezeCoinRequest {

    @NotBlank(message = "用户ID不能为空")
    private String residentId;

    private String frozenRecordId;

    @NotBlank(message = "解冻原因不能为空")
    @Size(min = 1, max = 200, message = "解冻原因长度需在1-200之间")
    private String reason;
}
