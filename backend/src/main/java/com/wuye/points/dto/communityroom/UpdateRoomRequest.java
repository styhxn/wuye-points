package com.wuye.points.dto.communityroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新房号请求（主要用于锁定/解锁）")
public class UpdateRoomRequest {

    /** vacant 置为空置 / locked 锁定 */
    @Schema(description = "房号状态：vacant 空置 / locked 锁定", example = "locked")
    private String status;

    @Schema(description = "楼层（可空，不传则不修改）", example = "1")
    private String floor;
}
