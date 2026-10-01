package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "商家服务小区项")
public class ServiceScopeCommunityItem {

    @Schema(description = "小区ID")
    private String communityId;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "是否已选中")
    private Boolean selected;
}
