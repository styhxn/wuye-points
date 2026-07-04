package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(description = "活动小组成员列表响应")
@Data
@Builder
public class ActivityGroupMemberListResponse {

    @Schema(description = "小组ID", example = "ag_xxx")
    private String groupId;

    @Schema(description = "小组名称", example = "跑步俱乐部")
    private String groupName;

    @Schema(description = "成员数量", example = "15")
    private Integer memberCount;

    @Schema(description = "成员列表")
    private List<ActivityGroupMemberItemResponse> list;
}
