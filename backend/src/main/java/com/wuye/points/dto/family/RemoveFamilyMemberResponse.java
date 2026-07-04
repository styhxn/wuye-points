package com.wuye.points.dto.family;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "移除家庭成员响应")
@Data
@Builder
public class RemoveFamilyMemberResponse {

    @Schema(description = "成员ID", example = "fm_xxx")
    private String memberId;

    @Schema(description = "家庭ID", example = "fam_xxx")
    private String familyId;

    @Schema(description = "移除时间", example = "2026-01-01T00:00:00.000Z")
    private String removedAt;

    @Schema(description = "积分处理方式", allowableValues = {"RETAIN", "TRANSFER", "CANCEL"})
    private String pointsHandling;

    @Schema(description = "币种处理方式", allowableValues = {"RETAIN", "TRANSFER", "CANCEL"})
    private String coinsHandling;
}
