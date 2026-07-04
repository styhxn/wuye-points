package com.wuye.points.dto.family;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "添加家庭成员响应")
@Data
@Builder
public class AddFamilyMemberResponse {

    @Schema(description = "成员ID", example = "fm_xxx")
    private String memberId;

    @Schema(description = "家庭ID", example = "fam_xxx")
    private String familyId;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "关系", example = "配偶")
    private String relation;

    @Schema(description = "是否为户主", example = "false")
    private Boolean isOwner;

    @Schema(description = "加入时间", example = "2026-01-01T00:00:00.000Z")
    private String joinedAt;
}
