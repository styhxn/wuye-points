package com.wuye.points.dto.admin.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 按主体批量登记分账个人接收方请求（管理端）。
 *
 * <p>前端<b>只传主体类型与主体 ID</b>，openid 由后端按 ownerId 反查住户账号取得，
 * 不经前端、不落表单，避免手填错号把钱打给陌生人。</p>
 */
@Data
@Schema(description = "按主体批量登记分账个人接收方请求")
public class AutoRegisterReceiversRequest {

    @Schema(description = "主体类型：RESIDENT/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER/"
            + "BUILDING_LEADER/COURIER/PROPERTY_MANAGER/MERCHANT（PROPERTY 走商户号，不支持自动登记）",
            example = "COURIER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "主体类型不能为空")
    private String ownerType;

    @Schema(description = "主体ID列表（角色表主键）", example = "[\"cm_001\",\"cm_002\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "主体ID列表不能为空")
    private List<String> ownerIds;

    @Schema(description = "是否直接置为已校验（默认 true；置 false 则该主体分账会被跳过）")
    private Boolean verified;
}
