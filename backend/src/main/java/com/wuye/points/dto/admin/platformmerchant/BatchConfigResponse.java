package com.wuye.points.dto.admin.platformmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "商家批量配置响应")
public class BatchConfigResponse {
    @Schema(description = "成功数量", example = "5")
    private int successCount;

    @Schema(description = "失败数量", example = "1")
    private int failCount;

    @Schema(description = "失败详情列表")
    private List<String> errors;
}
