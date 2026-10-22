package com.wuye.points.enums;

/**
 * 配送状态枚举（与 schema / docs/10 对齐）。
 *
 * <p>正式完成态为 {@link #completed}；{@link #delivered} 为历史部署遗留值，
 * 对外读写一律归一为 {@code completed}（见 {@code DisplayLabelUtil#normalizeDeliveryStatus}）。
 * {@link #merchant_self} / {@link #merchant_delivering} 为商家自配内部态，骑手不可见。</p>
 */
public enum DeliveryStatus {
    pending("待抢单"),
    accepted("已接单"),
    delivering("配送中"),
    completed("已完成"),
    failed("配送失败"),
    property_fallback("物业兜底"),
    merchant_self("商家自配待配送"),
    merchant_delivering("商家自配配送中"),
    /** 历史遗留完成态，等价 completed，仅兼容旧数据，禁止新写入 */
    delivered("已完成（历史兼容）"),
    /** 历史遗留退款态（旧部署退款冲正时写入），当前模型退款只改订单状态 */
    refunded("已退款（历史兼容）");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
