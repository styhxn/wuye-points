package com.wuye.points.support;

import java.util.Map;

/**
 * 平台交易明细的中文标签映射。
 *
 * <p>账本来自多张业务表，状态/类型枚举分散，统一在此处翻译，避免前端重复维护。
 * 未识别的枚举值原样返回，保证新增类型不会因为漏配而显示空白。</p>
 */
public final class TransactionLabels {

    public static final String GROUP_PAYMENT = "PAYMENT";
    public static final String GROUP_WALLET = "WALLET";
    public static final String GROUP_SETTLEMENT = "SETTLEMENT";
    public static final String GROUP_VALUE = "VALUE";

    private static final Map<String, String> CATEGORY_LABELS = Map.ofEntries(
            Map.entry("payment", "支付"),
            Map.entry("refund", "退款"),
            Map.entry("wallet", "钱包流水"),
            Map.entry("distribution", "订单分账"),
            Map.entry("split", "微信分账"),
            Map.entry("withdrawal", "提现"),
            Map.entry("transfer", "内部转账"),
            Map.entry("settlement", "结算"),
            Map.entry("point", "积分"),
            Map.entry("coin", "物业币"),
            Map.entry("canteen", "食堂定向余额")
    );

    private static final Map<String, String> GROUP_LABELS = Map.of(
            GROUP_PAYMENT, "支付与退款",
            GROUP_WALLET, "钱包与分账",
            GROUP_SETTLEMENT, "提现与结算",
            GROUP_VALUE, "积分与物业币"
    );

    private static final Map<String, String> DIRECTION_LABELS = Map.of(
            "income", "收入",
            "expense", "支出",
            "neutral", "不计收支"
    );

    private static final Map<String, String> UNIT_LABELS = Map.of(
            "CNY", "元",
            "POINT", "积分",
            "COIN", "物业币",
            "CNY_DIRECTED", "定向余额(元)"
    );

    private static final Map<String, String> PAYMENT_STATUS = Map.ofEntries(
            Map.entry("pending", "待支付"),
            Map.entry("paid", "已支付"),
            Map.entry("closed", "已关闭"),
            Map.entry("refunding", "退款中"),
            Map.entry("refunded", "已退款"),
            Map.entry("refund_failed", "退款失败"),
            Map.entry("failed", "支付失败"),
            Map.entry("amount_mismatch", "金额待对账")
    );

    private static final Map<String, String> WALLET_STATUS = Map.ofEntries(
            Map.entry("withdrawable", "已入账"),
            Map.entry("pending", "待结算"),
            Map.entry("frozen", "冻结"),
            Map.entry("receivable", "应收"),
            Map.entry("reversed", "已冲正")
    );

    private static final Map<String, String> SPLIT_STATUS = Map.ofEntries(
            Map.entry("pending", "待处理"),
            Map.entry("frozen_pending", "冻结待处理"),
            Map.entry("frozen", "已冻结"),
            Map.entry("finished", "分账完成"),
            Map.entry("cancelled", "已取消"),
            Map.entry("reverted", "已回退"),
            Map.entry("failed", "分账失败")
    );

    private static final Map<String, String> WITHDRAWAL_STATUS = Map.ofEntries(
            Map.entry("pending", "待审核"),
            Map.entry("pending_audit", "待审核"),
            Map.entry("approved", "已通过"),
            Map.entry("rejected", "已驳回"),
            Map.entry("completed", "已完成"),
            Map.entry("processing", "处理中"),
            Map.entry("success", "打款成功"),
            Map.entry("failed", "打款失败"),
            Map.entry("disburse_failed", "打款失败")
    );

    private static final Map<String, String> TRANSFER_STATUS = Map.ofEntries(
            Map.entry("pending", "待处理"),
            Map.entry("processing", "处理中"),
            Map.entry("success", "成功"),
            Map.entry("failed", "失败"),
            Map.entry("closed", "已关闭")
    );

    private static final Map<String, String> SETTLEMENT_STATUS = Map.ofEntries(
            Map.entry("settled", "已结算"),
            Map.entry("reversed", "已冲正")
    );

    private static final Map<String, String> VALUE_STATUS = Map.ofEntries(
            Map.entry("earn", "获得"),
            Map.entry("spend", "消费"),
            Map.entry("expire", "过期"),
            Map.entry("expired", "过期"),
            Map.entry("refund", "退回"),
            Map.entry("refunded", "已退回"),
            Map.entry("adjust", "调整"),
            Map.entry("transfer", "转移"),
            Map.entry("transfer_to_property", "转物业"),
            Map.entry("transfer_to_personal", "转个人"),
            Map.entry("grant", "发放"),
            Map.entry("reward", "奖励"),
            Map.entry("freeze", "冻结"),
            Map.entry("freeze_failed", "冻结失败"),
            Map.entry("unfreeze", "解冻"),
            Map.entry("recharge", "充值"),
            Map.entry("withdrawal", "提现"),
            Map.entry("withdrawal_deposit", "提现保证金"),
            Map.entry("deducted", "已扣减"),
            Map.entry("consumed", "已消费"),
            Map.entry("issued", "已发行"),
            Map.entry("cancelled", "已取消")
    );

    private static final Map<String, String> CANTEEN_STATUS = Map.of(
            "consume_deduct", "消费扣减",
            "refund_back", "退款退回"
    );

    private static final Map<String, String> VALUE_SOURCES = Map.ofEntries(
            Map.entry("order", "订单"),
            Map.entry("order_refund", "订单退款"),
            Map.entry("property_fee", "物业费"),
            Map.entry("on_time_payment_reward", "按时缴费奖励"),
            Map.entry("manual_adjust", "手工调整"),
            Map.entry("fee_clear", "物业费清零"),
            Map.entry("coin_expired", "物业币过期"),
            Map.entry("profile_completion", "资料完善"),
            Map.entry("reward_grant", "奖励发放"),
            Map.entry("pool_diff", "积分池差额"),
            Map.entry("exchange_diff", "兑换差额"),
            Map.entry("recharge", "充值"),
            Map.entry("withdrawal", "提现"),
            Map.entry("refund", "退回"),
            Map.entry("canteen", "食堂"),
            Map.entry("activity", "活动"),
            Map.entry("coupon", "优惠券")
    );

    private static final Map<String, String> PAYMENT_BIZ_TYPES = Map.ofEntries(
            Map.entry("order", "商品订单"),
            Map.entry("property_fee", "物业费"),
            Map.entry("coin_recharge", "物业币充值"),
            Map.entry("service_order", "服务订单"),
            Map.entry("service_subscription", "服务订阅"),
            Map.entry("activity_subscription", "活动订阅"),
            Map.entry("resident_shop_deposit", "业主小店保证金"),
            Map.entry("refund", "退款")
    );

    private static final Map<String, String> WALLET_ENTRY_TYPES = Map.ofEntries(
            Map.entry("merchant_goods_income", "商家商品收入"),
            Map.entry("merchant_delivery_subsidy", "商家配送补贴扣减"),
            Map.entry("platform_product_share", "平台商品分成"),
            Map.entry("platform_delivery_share", "平台配送分成"),
            Map.entry("property_product_share", "物业分成"),
            Map.entry("delivery_subsidy_cost", "配送补贴成本"),
            Map.entry("deficit_cost", "成本击穿"),
            Map.entry("coordinator_share", "统筹分成"),
            Map.entry("sector_leader_share", "板块负责人分成"),
            Map.entry("individual_leader_share", "个体负责人分成"),
            Map.entry("property_manager_share", "物业经理分成"),
            Map.entry("distribution_reversal", "分账冲正"),
            Map.entry("courier_delivery_income", "配送员收入"),
            Map.entry("merchant_delivery_fee_share", "商家配送费分成"),
            Map.entry("canteen_quota_fee", "食堂额度手续费"),
            Map.entry("distributor_wholesale_income", "一级代理货款收入")
    );

    private static final Map<String, String> WITHDRAWAL_TYPES = Map.ofEntries(
            Map.entry("merchant_withdrawal", "商家提现"),
            Map.entry("coin_withdrawal", "物业币兑换现金"),
            Map.entry("individual_leader", "个体负责人提现"),
            Map.entry("courier", "配送员提现"),
            Map.entry("coordinator", "统筹提现"),
            Map.entry("sector_leader", "板块负责人提现"),
            Map.entry("building_leader", "楼长提现"),
            Map.entry("resident_seller", "业主卖家提现"),
            Map.entry("property_manager", "物业经理提现")
    );

    private static final Map<String, String> OWNER_TYPES = Map.ofEntries(
            Map.entry("merchant", "商家"),
            Map.entry("platform", "平台"),
            Map.entry("property", "物业"),
            Map.entry("coordinator", "统筹"),
            Map.entry("sector_leader", "板块负责人"),
            Map.entry("individual_leader", "个体负责人"),
            Map.entry("building_leader", "楼长"),
            Map.entry("property_manager", "物业经理"),
            Map.entry("courier", "配送员"),
            Map.entry("resident", "住户")
    );

    private TransactionLabels() {
    }

    public static String categoryLabel(String category) {
        return CATEGORY_LABELS.getOrDefault(category, category);
    }

    public static String groupLabel(String group) {
        return GROUP_LABELS.getOrDefault(group, group);
    }

    public static String directionLabel(String direction) {
        return DIRECTION_LABELS.getOrDefault(direction, direction);
    }

    public static String unitLabel(String unit) {
        return UNIT_LABELS.getOrDefault(unit, unit);
    }

    public static String statusLabel(String category, String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        Map<String, String> map = switch (category == null ? "" : category) {
            case "payment", "refund" -> PAYMENT_STATUS;
            case "wallet" -> WALLET_STATUS;
            case "split" -> SPLIT_STATUS;
            case "withdrawal" -> WITHDRAWAL_STATUS;
            case "transfer" -> TRANSFER_STATUS;
            case "distribution", "settlement" -> SETTLEMENT_STATUS;
            case "point", "coin" -> VALUE_STATUS;
            case "canteen" -> CANTEEN_STATUS;
            default -> Map.of();
        };
        return map.getOrDefault(status, status);
    }

    public static String typeLabel(String category, String bizType) {
        if (bizType == null || bizType.isBlank()) {
            return "";
        }
        Map<String, String> map = switch (category == null ? "" : category) {
            case "payment", "refund" -> PAYMENT_BIZ_TYPES;
            case "wallet" -> WALLET_ENTRY_TYPES;
            case "withdrawal" -> WITHDRAWAL_TYPES;
            case "transfer", "split" -> OWNER_TYPES;
            case "distribution" -> Map.of("order_distribution", "订单分账");
            case "settlement" -> Map.of("resident_shop_settlement", "业主小店结算");
            case "point", "coin" -> VALUE_SOURCES;
            case "canteen" -> CANTEEN_STATUS;
            default -> Map.of();
        };
        return map.getOrDefault(bizType, bizType);
    }

    public static String ownerTypeLabel(String ownerType) {
        if (ownerType == null) {
            return "";
        }
        return OWNER_TYPES.getOrDefault(ownerType.toLowerCase(), ownerType);
    }
}
