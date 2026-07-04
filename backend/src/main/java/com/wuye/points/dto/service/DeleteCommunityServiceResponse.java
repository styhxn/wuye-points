package com.wuye.points.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "删除社区服务响应")
public class DeleteCommunityServiceResponse {

    @Schema(description = "ID", example = "svc_m8k2n4")
    private String id;

    @Schema(description = "状态", example = "已删除")
    private String status;
}
