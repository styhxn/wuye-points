package com.wuye.points.dto.family;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "家庭响应")
public class FamilyResponse {

    @Schema(description = "家庭ID", example = "family_001")
    private String id;

    @Schema(description = "家庭名称", example = "张三家庭")
    private String name;

    @Schema(description = "户主ID", example = "resident_001")
    private String ownerId;

    @Schema(description = "户主姓名", example = "张三")
    private String ownerName;

    @Schema(description = "物业公司ID", example = "property_001")
    private String propertyCompanyId;

    @Schema(description = "小区ID", example = "community_001")
    private String communityId;

    @Schema(description = "小区名称", example = "幸福家园")
    private String communityName;

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Schema(description = "房间号", example = "101")
    private String room;

    @Schema(description = "成员数量", example = "3")
    private Integer memberCount;

    @Schema(description = "积分余额", example = "1000")
    private Integer pointBalance;

    @Schema(description = "金币余额", example = "50.00")
    private BigDecimal coinBalance;

    @Schema(description = "投票权重", example = "3")
    private Integer voteWeight;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "家庭状态", example = "active")
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2024-01-01T00:00:00")
    private String updatedAt;
}
