package com.wuye.points.dto.communityroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "房号列表响应")
public class RoomListResponse {

    @Schema(description = "社区ID")
    private String communityId;

    @Schema(description = "房号列表")
    private List<RoomManageItemResponse> rooms;

    @Schema(description = "总数")
    private int total;
}
