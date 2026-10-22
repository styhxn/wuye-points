package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "论坛禁言请求（影子封禁：被禁用户无感知，仅自己可见）")
public class ForumBanRequest {

    @Size(max = 200, message = "禁言原因长度不能超过200")
    @Schema(description = "禁言原因（可选，管理端留痕）")
    private String reason;

    @Schema(description = "到期时间（ISO8601，如 2026-11-01T00:00:00；不传=永久禁言）")
    private String expiresAt;
}
