package com.wuye.points.dto.admin.operationlog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "操作日志项响应")
public class OperationLogItemResponse {

    @Schema(description = "日志ID", example = "LOG20240101001")
    private String id;

    @Schema(description = "操作员ID", example = "ADMIN001")
    private String operatorId;

    @Schema(description = "操作员姓名", example = "管理员")
    private String operatorName;

    @Schema(description = "操作员角色", example = "property_admin")
    private String operatorRole;

    @Schema(description = "物业公司ID", example = "COMP001")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称", example = "XX物业管理有限公司")
    private String propertyCompanyName;

    @Schema(description = "操作模块", example = "积分管理")
    private String module;

    @Schema(description = "操作类型", example = "发放积分")
    private String action;

    @Schema(description = "目标类型", example = "住户")
    private String targetType;

    @Schema(description = "目标ID", example = "RES001")
    private String targetId;

    @Schema(description = "操作前数据", example = "{\"balance\": 400}")
    private String beforeData;

    @Schema(description = "操作后数据", example = "{\"balance\": 500}")
    private String afterData;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "用户代理", example = "Mozilla/5.0")
    private String userAgent;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;
}
