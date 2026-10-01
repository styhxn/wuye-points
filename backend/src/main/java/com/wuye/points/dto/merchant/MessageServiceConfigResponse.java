package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "商家消息服务配置响应")
public class MessageServiceConfigResponse {

    @Schema(description = "是否开通")
    private Boolean enabled;

    @Schema(description = "服务半径")
    private String serviceRadius;

    @Schema(description = "服务分类ID列表")
    private List<String> categoryIds;
}
