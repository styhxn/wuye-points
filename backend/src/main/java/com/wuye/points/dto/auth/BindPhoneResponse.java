package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "绑定手机号响应")
public class BindPhoneResponse {

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "是否绑定成功", example = "true")
    private Boolean bound;

    @Schema(description = "绑定时间", example = "2026-01-01T00:00:00.000Z")
    private String boundAt;
}
