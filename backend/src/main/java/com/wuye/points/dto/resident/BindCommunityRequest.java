package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "住户绑定/完善小区请求")
@Data
public class BindCommunityRequest {

    @Schema(description = "小区ID（须以 com_ 开头且存在，且属于当前住户所属物业公司）",
            example = "com_demo001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "communityId不能为空")
    @Size(max = 32, message = "communityId长度不能超过32")
    private String communityId;
}
