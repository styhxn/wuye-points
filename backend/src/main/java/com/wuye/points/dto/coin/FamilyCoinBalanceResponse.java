package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "家庭物业币余额响应")
public class FamilyCoinBalanceResponse {

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;

    @Schema(description = "家庭名称", example = "张三家庭")
    private String familyName;

    @Schema(description = "家庭物业币余额", example = "500.00")
    private BigDecimal coinBalance;

    @Schema(description = "家庭成员数量", example = "3")
    private Integer memberCount;

    @Schema(description = "家庭成员列表")
    private List<Member> members;

    @Data
    @Builder
    @Schema(description = "家庭成员物业币信息")
    public static class Member {
        @Schema(description = "成员ID", example = "res_xxx")
        private String id;

        @Schema(description = "成员姓名", example = "张三")
        private String name;

        @Schema(description = "物业币余额", example = "500.00")
        private BigDecimal coinBalance;

        @Schema(description = "家庭关系", example = "户主")
        private String relation;
    }
}