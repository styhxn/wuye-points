package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "点赞结果")
public class ToggleLikeResponse {

    @Schema(description = "最新点赞数")
    private int likeCount;

    @Schema(description = "当前用户是否已点赞")
    private boolean liked;
}
