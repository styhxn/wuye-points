package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "商家服务范围响应")
public class ServiceScopeResponse {

    @Schema(description = "是否服务本公司全部小区")
    private Boolean serveAllCommunities;

    @Schema(description = "小区列表")
    private List<ServiceScopeCommunityItem> communities;
}
