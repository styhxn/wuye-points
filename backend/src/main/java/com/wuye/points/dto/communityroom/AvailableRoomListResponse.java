package com.wuye.points.dto.communityroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "可选房号列表响应")
public class AvailableRoomListResponse {

    @Schema(description = "社区ID", example = "com_xxxxxxxx")
    private String communityId;

    @Schema(description = "可选房号列表")
    private List<AvailableRoomItemResponse> items;

    @Schema(description = "可选房号总数", example = "20")
    private Integer totalAvailable;
}
