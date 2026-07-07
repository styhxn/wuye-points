package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "屏蔽邻居响应")
public class NeighborBlockResponse {

    @Schema(description = "是否屏蔽成功", example = "true")
    private Boolean success;

    @Schema(description = "操作类型", example = "blocked", allowableValues = {"blocked", "unblocked"})
    private String action;

    @Schema(description = "被屏蔽的邻居ID", example = "res_k3p5n2")
    private String blockedId;

    @Schema(description = "提示信息", example = "已屏蔽该邻居")
    private String message;
}
