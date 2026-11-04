package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "活动组成员中的单元楼长项")
@Data
@Builder
public class ActivityGroupBuildingLeaderItemResponse {

    @Schema(description = "成员记录ID", example = "agm_xxx")
    private String memberId;

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "住户姓名", example = "张阿姨")
    private String name;

    @Schema(description = "头像地址", example = "https://cdn.example.com/a.png")
    private String avatarUrl;

    @Schema(description = "负责人角色，固定 building_leader", example = "building_leader")
    private String leaderRole;
}
