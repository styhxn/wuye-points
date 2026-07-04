package com.wuye.points.dto.family;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "家庭成员项响应")
public class FamilyMemberItemResponse {

    @Schema(description = "成员ID", example = "member_001")
    private String id;

    @Schema(description = "成员姓名", example = "李四")
    private String name;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "与户主关系", example = "配偶")
    private String relation;

    @Schema(description = "是否为户主", example = "true")
    private Boolean isOwner;

    @Schema(description = "积分余额", example = "500")
    private Integer pointBalance;

    @Schema(description = "金币余额", example = "25.00")
    private BigDecimal coinBalance;

    @Schema(description = "成员状态", example = "active")
    private String status;

    @Schema(description = "加入时间", example = "2024-01-01T00:00:00")
    private String joinedAt;
}
