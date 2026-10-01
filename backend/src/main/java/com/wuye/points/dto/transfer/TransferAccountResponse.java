package com.wuye.points.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 收款账号绑定结果 / 列表项（阶段2）。
 *
 * <p>银行卡号 / 持卡人仅以加密形式存储，本响应不回显明文（敏感字段按最小暴露原则）。</p>
 */
@Schema(description = "四层树角色收款账号（阶段2）")
@Data
@Builder
public class TransferAccountResponse {

    @Schema(description = "账号ID")
    private String id;

    @Schema(description = "角色：PROPERTY/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER")
    private String ownerType;

    @Schema(description = "收款渠道：zero(微信零钱)/bank(银行卡)")
    private String channel;

    @Schema(description = "微信 openid（zero 渠道）")
    private String openid;

    @Schema(description = "开户银行全称（bank 渠道）")
    private String bankName;

    @Schema(description = "是否已核验可用")
    private Boolean verified;
}
