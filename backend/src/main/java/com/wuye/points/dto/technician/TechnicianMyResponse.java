package com.wuye.points.dto.technician;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "技工个人信息")
public class TechnicianMyResponse {

    @Schema(description = "技工住户ID")
    private String id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "专长/服务类目")
    private String specialty;

    @Schema(description = "账号状态")
    private String status;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称")
    private String propertyName;

    @Schema(description = "待处理工单数（未完成且未取消）")
    private Long taskCount;

    @Schema(description = "已完成工单数")
    private Long completedCount;
}
