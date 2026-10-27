package com.wuye.points.service;

import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;

import java.time.LocalDateTime;

/**
 * 积分转物业费资格判定（v8.23）。
 *
 * <p>三个入口共用同一口径：转物业接口、积分余额接口、住户「我的积分」接口。
 * 入住年限以 residents.created_at（账户创建时间）为基准，与转物业接口原有校验保持一致。</p>
 */
public final class PointTransferPolicy {

    private static final int DEFAULT_MIN_YEARS = 5;

    private PointTransferPolicy() {
    }

    public record Decision(boolean enabled, int minYears, boolean eligible, String blockedReason) {
    }

    public static Decision evaluate(PropertyCompany company, Resident resident) {
        int minYears = company != null && company.getPointTransferMinYears() != null
                ? company.getPointTransferMinYears() : DEFAULT_MIN_YEARS;
        boolean enabled = company != null && Boolean.TRUE.equals(company.getPointTransferEnabled());
        if (!enabled) {
            return new Decision(false, minYears, false, "积分转物业费暂未开放");
        }
        if (resident != null && resident.getCreatedAt() != null
                && resident.getCreatedAt().plusYears(minYears).isAfter(LocalDateTime.now())) {
            return new Decision(true, minYears, false, "入住未满" + minYears + "年，暂不可将积分转物业费");
        }
        return new Decision(true, minYears, true, null);
    }
}
