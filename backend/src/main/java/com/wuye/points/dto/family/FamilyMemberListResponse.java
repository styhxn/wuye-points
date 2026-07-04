package com.wuye.points.dto.family;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(description = "家庭成员列表响应")
@Data
@Builder
public class FamilyMemberListResponse {

    @Schema(description = "成员列表")
    private List<FamilyMemberItemResponse> list;
}
