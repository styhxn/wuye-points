package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "设置业主商户对外/不对外展示")
public class UpdateResidentMerchantVisibilityRequest {

    @NotBlank(message = "visibility不能为空")
    @Pattern(regexp = "public|private", message = "visibility须为 public 或 private")
    @Schema(description = "店铺对外展示：public（对外，任何人可查看）/private（不对外）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "public",
            allowableValues = {"public", "private"})
    private String visibility;
}
