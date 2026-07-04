package com.wuye.points.dto.family;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "家庭成员贡献响应")
@Data
@Builder
public class FamilyMemberContributionResponse {

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "家庭关系", example = "配偶")
    private String familyRelation;

    @Schema(description = "积分余额", example = "500")
    private int pointBalance;

    @Schema(description = "贡献占比", example = "0.35")
    private BigDecimal contributionPercent;
}
