package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
@Schema(description = "审核咨询师")
public class AuditConsultantRequest {

    @Schema(description = "approved/rejected（前端 auditResult 字段）")
    private String auditResult;

    @Schema(description = "approved/rejected（兼容 auditStatus 字段）")
    private String auditStatus;

    @Schema(description = "拒绝原因")
    private String reason;

    public String resolveAuditStatus() {
        if (StringUtils.hasText(auditResult)) {
            return auditResult.trim();
        }
        if (StringUtils.hasText(auditStatus)) {
            return auditStatus.trim();
        }
        return null;
    }
}
