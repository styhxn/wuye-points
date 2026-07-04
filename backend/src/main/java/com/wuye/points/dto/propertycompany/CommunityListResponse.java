package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "小区列表响应")
public class CommunityListResponse {

    @Schema(description = "物业公司ID", example = "res_b7m9x1")
    private String propertyCompanyId;

    @Schema(description = "小区列表")
    private List<CommunityItemResponse> list;
}
