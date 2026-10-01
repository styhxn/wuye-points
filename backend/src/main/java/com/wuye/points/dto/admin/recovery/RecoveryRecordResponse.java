package com.wuye.points.dto.admin.recovery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分账回退待追回台账记录（阶段2）。
 *
 * <p>电商收付通分账后退款：个人接收方（快递员 PERSONAL_OPENID）不可调微信回退，
 * 平台先行垫付并登记本台账，运营线下追回（对账催收 / 后续配送费抵扣）后核销。</p>
 */
@Schema(description = "分账回退待追回台账记录（阶段2）")
@Data
@Builder
public class RecoveryRecordResponse {

    @Schema(description = "台账ID")
    private String id;

    @Schema(description = "分账记录ID wechat_split_record.id")
    private String splitRecordId;

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "角色：COURIER 等")
    private String ownerType;

    @Schema(description = "角色ID（快递员ID）")
    private String ownerId;

    @Schema(description = "待追回金额（元）")
    private BigDecimal amount;

    @Schema(description = "pending(待追回)/recovered(已追回)/written_off(已核销)")
    private String status;

    @Schema(description = "备注（追回方式/线下处理记录）")
    private String remark;

    @Schema(description = "登记时间")
    private LocalDateTime createdAt;
}
