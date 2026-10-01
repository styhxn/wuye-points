package com.wuye.points.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 经纬度距离计算工具（Haversine 公式）。
 * 任意坐标为 null 时返回 null，调用方据此不展示距离。
 */
public final class GeoDistanceUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoDistanceUtil() {
    }

    /**
     * 计算两点球面距离（公里），保留 1 位小数。
     *
     * @return null 表示缺少坐标，无法计算
     */
    public static BigDecimal haversineKm(BigDecimal lat1, BigDecimal lon1,
                                         BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double km = EARTH_RADIUS_KM * c;
        return BigDecimal.valueOf(km).setScale(1, RoundingMode.HALF_UP);
    }
}
