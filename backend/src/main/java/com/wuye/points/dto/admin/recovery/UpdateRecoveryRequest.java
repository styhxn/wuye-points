package com.wuye.points.dto.admin.recovery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 追回台账处理请求（阶段2）：标记已追回 / 核销时填写处理备注。
 */
@Schema(description = "追回台账处理请求（阶段2）")
@Data
public class UpdateRecoveryRequest {

    @Schema(description = "处理备注（追回方式/核销原因，线下处理记录）", example = "已线下追回快递员配送费")
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
