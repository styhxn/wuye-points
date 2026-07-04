package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "邻居搜索响应")
public class NeighborSearchResponse {

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "楼栋", example = "1号楼")
    private String building;

    @Schema(description = "单元", example = "2单元")
    private String unit;

    @Schema(description = "房间号", example = "301")
    private String room;
}
