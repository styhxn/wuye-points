package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "积分转移请求")
public class TransferPointRequest {

    @NotBlank(message = "家庭ID不能为空")
    @Schema(description = "家庭ID", required = true, example = "fam_abc123")
    private String familyId;

    @NotBlank(message = "接收成员ID不能为空")
    @Schema(description = "接收成员ID", required = true, example = "res_c8n0y2")
    private String toMemberId;

    @NotNull(message = "转移数量不能为空")
    @Min(value = 1, message = "转移数量至少为1")
    @Schema(description = "转移数量", required = true, example = "100")
    private Integer amount;

    @Size(max = 200, message = "备注长度不能超过200")
    @Schema(description = "备注", example = "家庭内部积分转移")
    private String remark;
}
