package com.wuye.points.dto.communitypoint;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "小区建设积分池响应")
public class CommunityPointPoolResponse {

    @Schema(description = "积分池ID", example = "cpp_abc12345")
    private String id;

    @Schema(description = "小区ID", example = "com_abc12345")
    private String communityId;

    @Schema(description = "物业公司ID", example = "pc_abc12345")
    private String propertyCompanyId;

    @Schema(description = "当前余额", example = "5000.00")
    private BigDecimal balance;

    @Schema(description = "累计注入", example = "10000.00")
    private BigDecimal totalIn;

    @Schema(description = "累计消耗", example = "5000.00")
    private BigDecimal totalOut;

    @Schema(description = "更新时间", example = "2024-01-01 10:00:00")
    private String updatedAt;
}
