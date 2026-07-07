package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "屏蔽邻居请求")
public class NeighborBlockRequest {

    @NotBlank(message = "要屏蔽的邻居ID不能为空")
    @Schema(description = "要屏蔽的邻居ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "res_k3p5n2")
    private String blockedId;
}
