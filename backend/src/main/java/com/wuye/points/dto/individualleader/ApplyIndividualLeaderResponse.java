package com.wuye.points.dto.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplyIndividualLeaderResponse {

    @Schema(description = "申请ID", example = "ila_xxx")
    private String applicationId;

    @Schema(description = "申请状态", example = "pending")
    private String status;

    @Schema(description = "申请板块", example = "repair")
    private String sector;

    @Schema(description = "提示信息", example = "申请已提交，等待物业审核")
    private String message;
}
