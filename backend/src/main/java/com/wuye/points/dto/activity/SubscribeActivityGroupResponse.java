package com.wuye.points.dto.activity;

import com.wuye.points.payment.WechatJsPayParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "订阅活动小组响应")
public class SubscribeActivityGroupResponse {

    @Schema(description = "订阅ID", example = "sub_001")
    private String subscriptionId;

    @Schema(description = "小组ID", example = "grp_001")
    private String groupId;

    @Schema(description = "小组名称", example = "周末跑步团")
    private String groupName;

    @Schema(description = "订阅类型", example = "monthly")
    private String subType;

    @Schema(description = "订阅金额", example = "10.00")
    private BigDecimal amount;

    @Schema(description = "开始日期", example = "2024-01-01")
    private String startDate;

    @Schema(description = "结束日期", example = "2024-02-01")
    private String endDate;

    @Schema(description = "是否自动续费", example = "true")
    private Boolean autoRenew;

    @Schema(description = "订阅状态：pending_payment 待支付 / active 生效 / expired 已过期 / cancelled 已取消", example = "active")
    private String status;

    @Schema(description = "支付时间", example = "2024-01-01T00:00:00")
    private String paidAt;

    @Schema(description = "支付流水ID（paymentMethod=wechat 且已预下单时返回）")
    private String paymentRecordId;

    @Schema(description = "微信小程序调起支付参数（paymentMethod=wechat 时返回，前端原样透传 wx.requestPayment）")
    private WechatJsPayParams payParams;
}
