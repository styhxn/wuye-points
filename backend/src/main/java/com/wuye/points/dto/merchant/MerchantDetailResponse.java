package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "商家详情响应")
public class MerchantDetailResponse {

    @Schema(description = "商家ID", example = "merchant_001")
    private String id;

    @Schema(description = "平台商家ID", example = "platform_001")
    private String platformMerchantId;

    @Schema(description = "物业公司ID", example = "property_001")
    private String propertyCompanyId;

    @Schema(description = "商家名称", example = "幸福超市")
    private String name;

    @Schema(description = "商家描述", example = "社区便利超市")
    private String description;

    @Schema(description = "封面图片URL列表")
    private List<String> coverUrls;

    @Schema(description = "视频URL", example = "https://example.com/video.mp4")
    private String videoUrl;

    @Schema(description = "二维码URL", example = "https://example.com/qrcode.png")
    private String qrCodeUrl;

    @Schema(description = "商家等级", example = "gold")
    private String merchantLevel;

    @Schema(description = "等级权重", example = "100")
    private Integer levelWeight;

    @Schema(description = "商家类别", example = "超市")
    private String category;

    @Schema(description = "营业时间", example = "08:00-22:00")
    private String businessHours;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "关联住户ID（用于联系商家/进聊天）", example = "res_xxx")
    private String residentId;

    @Schema(description = "商家地址", example = "幸福路100号")
    private String address;

    @Schema(description = "配送费", example = "3.00")
    private java.math.BigDecimal deliveryFee;

    @Schema(description = "免配送费门槛金额", example = "50.00")
    private java.math.BigDecimal freeDeliveryThreshold;

    @Schema(description = "是否启用满额减免", example = "true")
    private Boolean freeDeliveryEnabled;

    @Schema(description = "满额减免承担方：none/merchant/property/platform", example = "merchant")
    private String freeDeliverySponsor;

    @Schema(description = "配送范围：in_community/out_community/both", example = "both")
    private String deliveryScope;

    @Schema(description = "多档配送费配置")
    private List<DeliveryFeeTier> deliveryFeeTiers;

    @Schema(description = "会员优惠价", example = "9.50")
    private java.math.BigDecimal memberDiscountPrice;

    @Schema(description = "金币返利比例", example = "0.1")
    private java.math.BigDecimal coinRebateRate;

    @Schema(description = "是否开启金币返利", example = "true")
    private Boolean coinRebateEnabled;

    @Schema(description = "排序权重", example = "100")
    private Integer rankOrder;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "商家状态", example = "active")
    private String status;

    @Schema(description = "商家来源原始值（platform/group_leader/technician/resident_shop 等）", example = "platform")
    private String merchantSource;

    @Schema(description = "店铺类型：merchant 普通商家；resident 业主小店", example = "merchant")
    private String shopType;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;

    @Schema(description = "是否一级代理（后台开通 is_distributor=1，可维护货源商品并接收业主小店货款）", example = "false")
    private Boolean isDistributor;

    @Schema(description = "更新时间", example = "2024-01-01T00:00:00")
    private String updatedAt;

    @Schema(description = "本店提现入口实际可用金额 = min(店铺可提现, 账号统一可提现)，仅商家本人/管理端查询时返回", example = "1280.50")
    private java.math.BigDecimal withdrawableAmount;

    @Schema(description = "本店铺可提现余额（店铺口径，未扣其他角色对统一账户的占用；v8.23 新增显式字段）", example = "1280.50")
    private java.math.BigDecimal shopWithdrawableAmount;

    @Schema(description = "账号统一可提现余额（楼长分成 + 业主卖货 + 名下商家，扣全部角色提现在途；v8.23 新增）", example = "1580.50")
    private java.math.BigDecimal accountWithdrawableAmount;

    @Schema(description = "盈利余额口径：shop=本店铺（固定值，供前端判断）", example = "shop")
    private String profitScope;

    @Schema(description = "结算模式：internal=内部收益（技师店/组长店/业主小店）；split=现金与物业币分账", example = "split")
    private String settlementMode;

    @Schema(description = "平台内部现金可提现（直连代收现金；服务商/电商现金已在商家微信商户号，不在此列）", example = "0.00")
    private java.math.BigDecimal cashWithdrawableAmount;

    @Schema(description = "物业币应收累计（已完成订单中商家应得但特约商户号无现金的部分）", example = "320.00")
    private java.math.BigDecimal coinReceivableAmount;

    @Schema(description = "物业币应收已结算金额", example = "120.00")
    private java.math.BigDecimal coinSettledAmount;

    @Schema(description = "物业币应收在途金额（pending/approved）", example = "50.00")
    private java.math.BigDecimal coinPendingAmount;

    @Schema(description = "物业币应收可结算余额", example = "150.00")
    private java.math.BigDecimal coinAvailableAmount;

    @Schema(description = "已由微信服务商/电商分账结算到商家商户号的现金（仅供对账，不走平台内部提现）", example = "880.00")
    private java.math.BigDecimal wechatCashSettledAmount;

    @Schema(description = "商家累计应得（商品金额 − 抽佣 − 商家承担的配送补贴）", example = "1200.00")
    private java.math.BigDecimal totalIncomeAmount;

    @Schema(description = "累计订单总数（不含已取消/已退款）", example = "128")
    private Long totalOrders;

    @Schema(description = "累计已完成订单金额（已完成订单 totalAmount 合计；仅商家本人/管理端查询时返回）", example = "12500.00")
    private java.math.BigDecimal totalRevenue;

    @Schema(description = "商家购分账户剩余积分（与 /merchant/points/* 同一账户）", example = "5000")
    private Integer pointBalance;

    @Schema(description = "积分兑换比（1元=X积分）；配置缺失时按系统默认 100 返回", example = "100.00")
    private java.math.BigDecimal pointExchangeRate;

    @Schema(description = "盈利余额（店铺口径，不含楼长分成/业主卖货收益；与 shopWithdrawableAmount 同值）", example = "1280.50")
    private java.math.BigDecimal profitBalance;

    @Schema(description = "提现是否被冻结（仅商家本人/管理端查询时返回）", example = "false")
    private Boolean withdrawableBlocked;

    @Schema(description = "商家商品列表")
    private List<MerchantProductItemResponse> products;
}
