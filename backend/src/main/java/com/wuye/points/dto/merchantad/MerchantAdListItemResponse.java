package com.wuye.points.dto.merchantad;

import com.wuye.points.payment.WechatJsPayParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "商家广告列表项")
public class MerchantAdListItemResponse {

    private String id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private String productId;
    private Integer recipientCount;

    @Schema(description = "展示天数")
    private Integer durationDays;

    @Schema(description = "投放开始日期")
    private String startDate;

    @Schema(description = "投放结束日期")
    private String endDate;

    @Schema(description = "支付金额（0=免费额度投放）")
    private BigDecimal payAmount;

    @Schema(description = "支付方式: free/wechat")
    private String paymentMethod;

    @Schema(description = "支付状态：unpaid 待支付 / paid 已支付（含免费额度）/ refunded 已退款（保留值，当前无广告退款流程）")
    private String payStatus;

    @Schema(description = "列表展示状态：pending_payment 待支付 / active 投放中 / ended 已结束 / cancelled 已取消")
    private String status;

    @Schema(description = "剩余投放天数；未支付或已结束时为 0", example = "3")
    private Integer remainingDays;

    @Schema(description = "是否可继续支付（待支付/支付超时取消，且为微信现金广告）", example = "true")
    private Boolean canRepay;

    private String createdAt;

    @Schema(description = "最近一笔支付流水ID（含待支付/已关闭流水，补付接口使用）")
    private String paymentRecordId;

    @Schema(description = "微信小程序调起支付参数（paymentMethod=wechat 时返回，前端原样透传 wx.requestPayment）")
    private WechatJsPayParams payParams;
}
