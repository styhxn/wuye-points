package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "开通/配置商家消息服务请求")
public class UpdateMessageServiceRequest {

    @NotNull
    @Schema(description = "是否开通", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enabled;

    @Schema(description = "服务半径：1km/3km/5km/district/city")
    private String serviceRadius;

    @Schema(description = "服务分类ID列表")
    private List<String> categoryIds;
}
