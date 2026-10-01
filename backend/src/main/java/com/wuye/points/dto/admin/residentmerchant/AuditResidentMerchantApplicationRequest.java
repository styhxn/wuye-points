package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
@Schema(description = "审核业主商户申请")
public class AuditResidentMerchantApplicationRequest {

    @Schema(description = "approved/rejected（前端 auditResult 字段）")
    private String auditResult;

    @Schema(description = "approved/rejected（兼容 auditStatus 字段）")
    private String auditStatus;

    @Schema(description = "审核备注（前端 reason 字段）")
    private String reason;

    @Schema(description = "审核备注（兼容 auditRemark 字段）")
    private String auditRemark;

    public String resolveAuditStatus() {
        if (StringUtils.hasText(auditResult)) {
            return auditResult.trim();
        }
        if (StringUtils.hasText(auditStatus)) {
            return auditStatus.trim();
        }
        return null;
    }

    public String resolveAuditRemark() {
        if (StringUtils.hasText(auditRemark)) {
            return auditRemark.trim();
        }
        if (StringUtils.hasText(reason)) {
            return reason.trim();
        }
        return null;
    }
}
