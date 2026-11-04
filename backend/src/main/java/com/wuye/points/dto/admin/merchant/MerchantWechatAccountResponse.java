package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 管理端 — 商家特约商户号绑定信息。 */
@Schema(description = "商家特约商户号绑定信息")
@Data
@Builder
public class MerchantWechatAccountResponse {

    private String id;
    private String merchantId;
    private String subMchid;
    private String merchantName;
    /** active / auditing / frozen */
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
