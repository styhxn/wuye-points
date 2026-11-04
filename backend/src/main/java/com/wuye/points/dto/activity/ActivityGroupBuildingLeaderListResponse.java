package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(description = "活动组内单元楼长列表响应")
@Data
@Builder
public class ActivityGroupBuildingLeaderListResponse {

    @Schema(description = "小组ID", example = "ag_xxx")
    private String groupId;

    @Schema(description = "小组名称", example = "跑步俱乐部")
    private String groupName;

    @Schema(description = "组内楼长数量", example = "2")
    private Integer buildingLeaderCount;

    @Schema(description = "组内楼长列表（无楼长时为空数组）")
    private List<ActivityGroupBuildingLeaderItemResponse> list;
}
