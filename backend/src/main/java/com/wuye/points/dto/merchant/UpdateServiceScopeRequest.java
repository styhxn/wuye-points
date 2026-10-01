package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "更新商家服务范围请求")
public class UpdateServiceScopeRequest {

    @NotNull
    @Schema(description = "是否服务本公司全部小区", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean serveAllCommunities;

    @Schema(description = "选中的小区ID列表（serveAllCommunities=false 时必填）")
    private List<String> communityIds;
}
