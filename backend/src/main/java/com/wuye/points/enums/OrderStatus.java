package com.wuye.points.enums;

public enum OrderStatus {
    pending("待支付"),
    paid("已支付"),
    delivering("配送中"),
    completed("已完成"),
    cancelled("已取消"),
    refunding("退款中"),
    refunded("已退款");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
